# Phase 4 — Choix techniques et matrice d’outils

Le client Light est Android natif (Kotlin, Jetpack Compose), avec Firebase AI et un transport HTTP compatible. Aucun runtime Node/React existant dans l’APK. Les poids ci-dessous sont les **tailles npm décompressées publiées** (`dist.unpackedSize` de la version indiquée), sans dépendances transitives, minification ni mesure de l’APK. Les tailles de bibliothèque ne sont pas des budgets de téléchargement ou de mémoire.

| Outil / source | Version npm et taille décompressée | Compatibilité et décision |
| --- | ---: | --- |
| [Vercel AI SDK](https://github.com/vercel/ai) | `ai` 7.0.177 · 8,59 Mio | SDK TypeScript, contrat Zod ; pertinent pour un éventuel backend d’orchestration. Ne pas l’ajouter au client natif. Conserver Firebase/HTTP et un contrat Kotlin unique. |
| [AI Elements](https://github.com/vercel/ai-elements) | `ai-elements` 1.9.1 · 8,9 Kio **CLI uniquement** | Les composants sont distribués par registre shadcn/React : poids réel des composants et dépendances non inclus. Étudier les états d’interface, pas de dépendance dans Compose. |
| [react-markdown](https://github.com/remarkjs/react-markdown) et [remark-math / rehype-katex](https://github.com/remarkjs/remark-math) | 10.1.0 · 51,4 Kio ; 6.0.0 · 12,6 Kio ; 7.0.1 · 15,7 Kio | `react-markdown` requiert React. Choix pour le premier lot : [CommonMark Java](https://github.com/commonmark/commonmark-java/tree/commonmark-parent-0.24.0) et rendu Compose ; le texte du modèle ne devient pas du HTML exécutable. |
| [KaTeX](https://github.com/KaTeX/KaTeX) | 0.19.1 · 3,86 Mio | Web, polices et CSS. À tester séparément pour les mathématiques, sans autoriser le réseau, le HTML arbitraire ou les options de confiance. |
| [Shiki](https://github.com/shikijs/shiki) | 4.6.0 · 588 Kio (hors moteurs, thèmes et grammaires) | JS ; garder police monospace et copie natives d’abord, puis évaluer un sous-ensemble de langages et mesurer l’APK. |
| [Mermaid](https://github.com/mermaid-js/mermaid) | 12.3.0 · 117,90 Mio | Moteur de rendu Web ; pas dans le chat général. Prototype de rendu isolé avec configuration stricte, diagrammes bornés, aucune interaction réseau ou JS fourni par le modèle. |
| [Excalidraw](https://github.com/excalidraw/excalidraw) | 0.18.1 · 44,63 Mio | React/ReactDOM. Distinguer un aperçu statique d’une scène validée d’un éditeur interactif ; pas de chargement automatique de l’éditeur dans le chat. |
| [OpenUI](https://github.com/thesysdev/openui) | `react-lang` 1.1.1 · 687 Kio ; `react-ui` 0.18.9 · 16,02 Mio | Schéma/Lang et composants React (dépendances distinctes). Ne pas exécuter de code émis par le modèle. Garder une liste de composants Compose autorisés avec validation des données ; évaluer un adaptateur OpenUI après ce contrat, pas de moteur JS maintenant. |

## Matrice fonctions → outils

Dans ce premier lot, les champs ci-dessous sont validés par un contrat Kotlin commun aux déclarations Firebase et HTTP et à l’exécution. Remplacer Zod dans le client natif par un contrôle explicite des objets, types, tailles, listes et dates. Validation avant aperçu et avant demande d’autorisation. Aucun outil arbitraire ni exécution de scripts.

| Fonction | Outil | Champs obligatoires | Autorisation |
| --- | --- | --- | --- |
| Lire le planning | `list_tasks` | `filter` (enum), date facultative | Capacité de lecture |
| Lire matières/projets/habitudes/échéances | `list_study_data` | Aucun | Capacité de lecture |
| Montrer une proposition | `present_study_plan` | `title`, `summary`, `items` (1 à 8 objets fixes) | Capacité d’aperçu ; aucune écriture |
| Créer une tâche | `create_task` | Titre, matière (vide si aucune), catégorie, priorité, échéance, durée | Capacité + confirmation |
| Marquer terminée | `complete_task` | `taskQuery` | Capacité + confirmation |
| Supprimer une tâche | `delete_task` | `taskQuery` (titre exact côté exécuteur) | Capacité désactivée par défaut + confirmation destructive |
| Créer une matière | `add_course` | `code`, `title` | Capacité + confirmation |
| Créer un projet | `create_project` | `title`, début et échéance ordonnés | Capacité + confirmation |
| Régler le dégradé | `set_gradient_theme` | `gradientId` dans le catalogue | Capacité + confirmation |
| Démarrer Focus | `start_focus_timer` | `mode` dans la liste | Capacité + confirmation |
| Créer une habitude | `create_habit` | `title` | Capacité + confirmation |
| Créer une échéance | `create_countdown` | `title`, `targetDateIso` | Capacité + confirmation |
| Générer une image | À définir | Prompt, fournisseur choisi, destination bibliothèque | Pré-requis : bibliothèque de phase 3, coffre local et contrat fournisseur |
| Recherche Tavily / Exa | À définir | Requête bornée et fenêtre temporelle optionnelle | Pré-requis : clés locales, réglage explicite et sources visibles ; pas de navigation arbitraire |
| Créer un dossier / artefact | À définir | Nom, type autorisé, dossier de destination | Pré-requis : bibliothèque isolée ; confirmation d’écriture |

## Compétences Anthropic (inventaire préalable)

Le [dépôt anthropics/skills](https://github.com/anthropics/skills/tree/main/skills) contient notamment `pdf`, `doc-coauthoring`, `frontend-design`, `canvas-design`, `docx`, `pptx` et `xlsx`. Pour étudiants, PDF et rédaction collaborative sont prioritaires ; pour travail/design, co-rédaction et design frontend. Vérifier la licence de chaque compétence avant redistribution ; plusieurs exemples portent des conditions distinctes. Les `SKILL.md` et scripts ne sont pas importés automatiquement. L’approche prévue est de rédiger des fiches courtes qui ne promettent que les outils Light réellement disponibles, avec activation explicite. Cette intégration reste à faire.
