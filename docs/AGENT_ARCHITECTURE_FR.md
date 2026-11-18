# Architecture de Light Agent : capacités et streaming

## Décision

Light conserve une interface native Jetpack Compose et Kotlin Coroutines/Flow. Pour le fournisseur Firebase/Gemini, le streaming de texte utilise `Chat.sendMessageStream()` de **Firebase AI Logic**. Les fragments sont intégrés au message assistant existant pendant la génération; les appels d'outils restent exécutés par l'application et chaque outil est refusé si la capacité correspondante est désactivée.

Les fournisseurs compatibles OpenAI actuellement gérés par Light gardent leur chemin HTTP existant et ne diffusent pas encore leurs fragments : ils affichent leur réponse une fois reçue. Le premier parcours réellement progressif est donc le parcours Firebase. Une prochaine évolution multi-fournisseur devra implémenter et tester le protocole SSE du fournisseur au moyen d'une interface Kotlin/Flow commune, en traitant séparément les événements de contenu, d'appel d'outil, d'erreur et de fin. Aucun SDK JavaScript ne doit être introduit dans l'application Android pour cela.

## CopilotKit, AG-UI et validation humaine

CopilotKit apporte une bonne référence de conception, mais son SDK UI CopilotKit est orienté interfaces React/Angular. La partie réutilisable pour une app Android est le **protocole AG-UI** (événements bidirectionnels et streaming), et il existe désormais un client Kotlin communautaire `com.ag-ui.community:kotlin-client`. Celui-ci sait consommer un serveur AG-UI en streaming et gérer outils côté client; il ne remplace pas à lui seul le serveur qui parle ce protocole ni les appels Firebase AI Logic déjà présents dans Light. La migration vers AG-UI impliquerait un adaptateur/serveur et mérite un prototype séparé avant de remplacer le transport actuel.

Light reprend dès maintenant le principe **controlled UI / human-in-the-loop** sans dépendre du runtime web de CopilotKit : chaque appel qui écrit ou modifie quelque chose est suspendu dans Kotlin, et une carte Compose affiche action, champs concernés et boutons Autoriser/Refuser. Aucun effet métier n'est exécuté avant « Autoriser ». Si aucune carte de validation n'est définie pour un futur outil d'écriture, l'outil est bloqué par défaut. Les outils de lecture seuls ne déclenchent pas de carte.

Le premier usage de **declarative UI à schéma fixe** est `present_study_plan` : l'agent remplit un modèle strict (titre, résumé, jusqu'à huit étapes avec détail, date, durée et priorité). Light ne rend que ces types de données dans un composable Compose prédéfini; aucun composant, code, lien d'action ou HTML provenant du modèle n'est interprété. Cet aperçu est informatif et n'enregistre rien. Pour créer ensuite des tâches, l'agent doit appeler les outils métier, qui déclenchent séparément les cartes contrôlées de validation.

Pour la suite, le bon ordre est :

1. **Controlled UI** pour toute action Light (choix d'un composant et champs connus), en particulier créations, validation, Focus, thème et suppressions.
2. **Declarative UI** éventuellement pour afficher un plan ou un formulaire composé de composants explicitement catalogués. Commencer par un schéma fixe et valider chaque action de formulaire avec les mêmes gardes Kotlin.
3. **Open UI / UI ouverte** seulement pour un besoin distinct et dans un bac à sable. Un HTML ou une app tierce ne doit jamais recevoir un accès direct aux repositories Light.

À noter : **A2UI** est un format déclaratif Agent-to-UI, **AG-UI** est le protocole d'interaction Agent↔Utilisateur, et **Open-JSON-UI** est un autre format structuré. Ils ne sont pas interchangeables. Le nom « Open UI » peut aussi désigner la génération ouverte de surfaces HTML/JavaScript; pour Light, ce niveau de liberté n'est pas nécessaire pour faire approuver des opérations simples.

## Comparaison des options

| Option | Compatibilité Android/Kotlin | Streaming | Place dans Light |
| --- | --- | --- | --- |
| **Firebase AI Logic** | SDK Android Kotlin déjà employé par Light | `sendMessageStream()` fournit un `Flow<GenerateContentResponse>` | Choix retenu pour Gemini/Firebase : faible changement, flux natif, intégration directe avec les outils actuels. |
| **AG-UI Kotlin client** | Client Kotlin communautaire, Android/JVM/KMP; les releases consultées listent `kotlin-client` 0.5.0 | Flux d'événements AG-UI et outils côté client | Piste forte pour une future couche de transport standardisée et les interruptions HITL; nécessite un backend compatible AG-UI. Pas encore intégré ni vérifié au build Android de Light. |
| **Google GenAI / Gemini API** | API et bibliothèques JVM possibles; à qualifier pour Android et la configuration de clés | Streaming disponible selon SDK/backend | Adapté à un client Gemini autonome, mais ne remplace pas Compose, la gestion des capacités ni la logique des outils. Firebase AI Logic est préférable ici car le projet l'utilise déjà. |
| **CopilotKit UI** | SDK UI orienté React/Angular (les concepts se transposent, pas les composants/hooks à Kotlin) | Cartes de rendu de tool calls, `useHumanInTheLoop` et interruptions dans une UI web | Bonne référence d'expérience et de garde humaine. Non ajouté comme dépendance Compose; Light utilise une carte Compose native au même point de contrôle. |
| **Vercel AI SDK / AI SDK UI** | SDK UI centré TypeScript/React, pas un composable Kotlin/Compose | Flux UI/protocoles côté JavaScript, généralement avec un endpoint de serveur | Non retenu comme dépendance Android. Ne devient pertinent que si Light adopte séparément un backend TypeScript, sans envoyer les secrets fournisseur au client et sans transformer l'UI native en WebView. |
| **JetBrains Koog** | Framework d'agents Kotlin/JVM, plus idiomatique que les frameworks Java pour Kotlin | Prend en charge des flux d'événements et des outils | Candidat à un prototype ultérieur si Light veut remplacer son orchestration multi-fournisseur. Pas intégré : vérifier les versions, le graphe de dépendances Android, les fournisseurs, la gestion des secrets et les appels d'outils avant migration. |
| **LangChain4j** | Framework Java/JVM, interopérable avec Kotlin | Interfaces de génération en flux | Alternative pour un service JVM ou une orchestration plus large; moins idiomatique et probablement plus lourde que l'implémentation native actuelle pour ce besoin. |

## Capacités et garde-fous

- Les interrupteurs dédiés de **Explorer → Capacités de l'agent** partagent la préférence DataStore déjà utilisée par les réglages de Light Agent.
- Le catalogue d'outils et le contrôle exécuté côté application reposent sur la même clé de capacité. Masquer un outil au modèle ne constitue pas la seule protection : `executeTool` vérifie aussi l'autorisation au moment de l'exécution.
- Le contexte envoyé au fournisseur masque aussi les matières/projets, le nombre de tâches et le thème si les capacités de lecture correspondantes sont désactivées. `list_study_data` ne renvoie que des titres, dates et catégories utiles; il n'expose ni notes ni descriptions personnelles.
- Les fonctions disponibles comprennent la consultation/création/validation de tâches, la suppression de tâche (désactivée par défaut, et refusée quand plusieurs titres correspondent), l'ajout de matière/projet/habitude/échéance, le changement de thème et le démarrage de Focus.
- L'agent ne reçoit pas d'outil de suppression pour les habitudes, échéances, matières ou projets. La page explique la portée des capacités.
- L'extraction de pièces jointes reste locale. Le contenu d'un document n'est envoyé au fournisseur que par le parcours avec consentement explicite déjà présent dans le chat. Voir aussi `docs/PRODUCTION_READINESS_FR.md`.

## Références

- [Firebase AI Logic : chat et streaming Kotlin](https://firebase.google.com/docs/ai-logic/chat)
- [Référence Android Firebase AI : `Chat.sendMessageStream`](https://firebase.google.com/docs/reference/android/com/google/firebase/ai/Chat)
- [Gemini API : génération de texte et streaming](https://ai.google.dev/gemini-api/docs/text-generation)
- [CopilotKit : generative UI et types contrôlé/déclaratif/ouvert](https://docs.copilotkit.ai/concepts/generative-ui-overview)
- [CopilotKit : human-in-the-loop et interruptions](https://docs.copilotkit.ai/ms-agent-dotnet/human-in-the-loop/useInterrupt)
- [AG-UI : protocole Agent–Utilisateur](https://docs.ag-ui.com/introduction)
- [AG-UI Kotlin SDK : documentation et quick start](https://github.com/ag-ui-protocol/ag-ui/blob/main/docs/sdk/kotlin/overview.mdx) et [releases](https://github.com/ag-ui-protocol/ag-ui/releases)
- [A2UI : UI déclarative](https://docs.copilotkit.ai/generative-ui/a2ui)
- [Open-JSON-UI](https://docs.copilotkit.ai/strands/generative-ui/open-json-ui)
- [Vercel AI SDK UI : chatbot](https://ai-sdk.dev/docs/ai-sdk-ui/chatbot) et [protocoles de flux](https://ai-sdk.dev/docs/ai-sdk-ui/stream-protocol)
- [JetBrains Koog : dépôt](https://github.com/JetBrains/koog) et [documentation](https://docs.koog.ai/quickstart/)
- [LangChain4j : services IA](https://docs.langchain4j.dev/tutorials/ai-services/)
