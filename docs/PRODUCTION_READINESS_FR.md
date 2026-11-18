# Light — validation avant publication

## État vérifiable dans ce workspace

- Les changements sont sur une branche de développement; aucune publication ni aucun déploiement Firebase ne doit être lancé depuis ce workspace.
- La connexion Google demeure volontairement en pause jusqu'à validation visuelle explicite de la propriétaire. Ne pas désactiver `googleAuthPausedForTesting` avant cet accord.
- Le code d'extraction locale des documents, son intégration au chat et les dépendances PDFBox Android / ML Kit sont présents, mais **non compilés et non testés** dans cet environnement. Aucun test fournisseur réel n'a été exécuté.
- Les documents sont extraits sur l'appareil. Le texte extrait n'est transmis au fournisseur IA qu'après confirmation à l'envoi. Cela n'est pas un mode IA local : l'agent sélectionné (Gemini, Agnes ou autre fournisseur configuré) reçoit alors le texte.
- Formats pris en charge par le parseur : MD/Markdown/TXT, PDF, DOCX, XLSX et PNG. Les anciens fichiers `.doc` / `.xls` doivent être convertis. Limites courantes : 15 Mo par fichier, 100 pages PDF, 30 000 caractères extraits par fichier, 4 fichiers et 60 000 caractères de documents par message.
- PDF : séparation de colonnes heuristique selon la position du texte; pages sans couche texte (y compris pages scannées au milieu d'un PDF mixte) traitées par OCR local. DOCX/XLSX : séparateurs de cellules et références conservés; dates Excel formatées interprétées tout en gardant le serial brut. Les mises en page complexes/fusionnées et OCR illisible restent à faire contrôler par l'utilisateur.
- L'affichage des prix dépend des métadonnées retournées par le catalogue fournisseur. Agnes, Groq, OpenAI, Anthropic et Firebase ne doivent pas être présentés comme gratuitement utilisables sans conditions vérifiées; les estimations de coûts doivent être distinguées de la facture fournisseur.

## Actions Firebase à faire par l'agent Google

Aucune modification Firebase ne doit être déployée par cet agent. L'agent Google doit examiner et valider tout changement Firebase nécessaire avant mise en production.

1. **Google Sign-In / OAuth**
   - Activer Google comme fournisseur dans Firebase Authentication.
   - Vérifier les empreintes SHA-1 et SHA-256 de la clé de signature Play (et de la clé de test interne) dans Firebase / Google Cloud; vérifier le client OAuth Web attendu par Credential Manager.
   - Fournir le `google-services.json` correspondant à `com.aistudio.elanstudy.vkmrxz` dans `app/` selon la politique du projet.
   - Tester connexion, reconnexion, annulation, déconnexion, compte sans profil et retour au mode local.
   - Ne dé-pauser Google qu'après accord visuel explicite; vérifier la préférence persistée sur une installation propre de production.

2. **App Check**
   - Le code utilise Debug pour les builds Debug et Play Integrity pour les builds Release.
   - Activer Play Integrity pour la bonne application, associer l'app Play et vérifier la signature de production.
   - Ne jamais distribuer le jeton Debug; inspecter les métriques avant d'imposer App Check sur Firestore / Firebase AI Logic.

3. **Cloud Firestore**
   - Le client vise la base nommée `ai-studio-android-lan-cb9bb915-7486-4610-a057-db58c7e693a7`, pas nécessairement `(default)`. Confirmer qu'elle existe dans le bon projet.
   - Faire examiner les règles `firestore.rules`, exécuter `firestore.test.js` sur Firestore Emulator, valider propriétaire/non-propriétaire et authentifié/non-authentifié, puis tester tâches, projets et filtres de dates.
   - Ne déployer les règles et index qu'après validation de l'agent Google. Ne pas ouvrir les règles pour contourner un échec.

4. **Firebase AI Logic et fournisseurs**
   - Confirmer l'activation de Firebase AI Logic, le modèle disponible pour le projet/région, App Check et les quotas.
   - Tester séparément les catalogues et appels API Anthropic, OpenAI, OpenRouter, Agnes, Groq et les endpoints personnalisés avec comptes/clés de test, erreurs d'authentification et données de prix actuelles.
   - Vérifier les conditions d'usage, quotas, rétention et tarification de chaque fournisseur; ne jamais coder en dur une promesse de gratuité non vérifiée.

## Vérifications locales requises avant release

1. Fournir le wrapper Gradle et un JDK compatible; exécuter `:app:testDebugUnitTest`, `:app:lint` et `:app:assembleRelease`.
2. Ajouter/exécuter des tests sur l'extraction de tableaux PDF à texte positionné, PDF scanné et mixte, DOCX avec tableaux, XLSX avec chaînes partagées/formules/dates, PNG OCR, fichiers corrompus et limites de taille. Contrôler les résultats sur un jeu de documents réel autorisé qui n'a pas servi au développement; vérifier manuellement toute structure ambiguë.
3. Exécuter les tests `firestore.test.js` avec Firestore Emulator et les dépendances `@firebase/rules-unit-testing` / `firebase`.
4. Tester sur appareil les documents supportés, annulation du consentement, envoi au fournisseur, clés API invalides, URL non HTTPS, erreurs HTTP, usage/coût inconnu, redémarrage hors ligne, performances et mémoire sur des appareils de gamme modeste.
5. Vérifier secrets de signature Play, `versionCode`, icônes/adaptive icon, déclaration de confidentialité (transmission facultative du prompt, planning et texte des documents au fournisseur choisi), fiche Play Store, captures et consentements.
6. Recueillir l'accord visuel explicite avant de réactiver Google; tester ensuite les parcours d'authentification de production.
