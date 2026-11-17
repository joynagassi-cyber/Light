# Light — validation avant publication

## État

La validation visuelle n'a pas encore été donnée. La connexion Google reste volontairement en pause dans l'environnement de prévisualisation. **Ne publier ni basculer l'authentification de production avant l'accord visuel de la propriétaire.** Le build Android et la suite Firestore n'ont pas été exécutés dans ce workspace (pas de `gradlew`, pas de distribution Gradle locale et dépendances Node de test absentes).

## Actions Firebase à faire par l'agent Google

1. **Google Sign-In / OAuth**
   - Activer Google comme fournisseur dans Firebase Authentication.
   - Vérifier les empreintes SHA-1 et SHA-256 de la clé de signature Play (et de la clé de test interne) dans le projet Firebase / Google Cloud; vérifier le client OAuth Web attendu par Credential Manager.
   - Fournir le `google-services.json` correspondant à l'application `com.aistudio.elanstudy.vkmrxz` dans `app/` sans le publier dans le dépôt si la politique du projet l'exclut.
   - Tester connexion, reconnexion, annulation, déconnexion, compte sans profil et retour au mode local.
   - Après l'accord visuel seulement, désactiver le mode « Connexion Google en pause » dans les réglages de l'app. La préférence est persistée localement et sa valeur par défaut actuelle est `true` : vérifier explicitement la valeur sur l'installation de production.

2. **App Check**
   - Le code utilise maintenant le fournisseur Debug pour les builds Debug et **Play Integrity** pour les builds Release.
   - Activer Play Integrity dans App Check pour la bonne application Android, lier l'application Play et enregistrer l'empreinte de signature de production.
   - Ne jamais distribuer le jeton App Check Debug et vérifier les métriques avant d'imposer l'application d'App Check sur Firestore / Firebase AI Logic.

3. **Cloud Firestore**
   - Le client vise la base nommée `ai-studio-android-lan-cb9bb915-7486-4610-a057-db58c7e693a7` (`app/src/main/res/values/firebase_applet_config.xml`), pas nécessairement `(default)`. Confirmer que cette base existe dans le bon projet.
   - Déployer `firestore.rules` vers cette base et exécuter `firestore.test.js` sur Firestore Emulator. Vérifier ensuite les requêtes réelles (notamment tâches, projets et filtres par dates) et créer les index composites signalés par le SDK si nécessaire.
   - Contrôler les règles en compte authentifié/non authentifié et propriétaire/non-propriétaire. Ne pas ouvrir les règles pour contourner un échec.

4. **Firebase AI Logic**
   - Le fournisseur par défaut de Light reste Gemini via Firebase AI Logic. Vérifier l'activation du produit, le modèle disponible dans la région/projet et l'application des règles App Check/quotas.
   - Le fournisseur « API compatible » ajouté est facultatif et direct depuis l'appareil; il ne requiert aucune collection Firestore, fonction Cloud ou migration. Sa clé reste chiffrée dans Android Keystore et n'est pas envoyée à Firebase. L'URL configurée doit fournir une API compatible OpenAI Chat Completions en HTTPS.

## Capacités Agent livrées dans cette passe

- Fournisseur Gemini Firebase ou endpoint HTTPS compatible OpenAI Chat Completions (URL, modèle, clé API personnelle).
- Niveaux de réflexion Rapide / Équilibré / Approfondi.
- Capacités activables séparément : lecture/création/validation/suppression des tâches, gestion de matières, création de projets, changement de thème et démarrage Focus.
- Compétences personnelles enregistrées localement (instructions réutilisables que l'agent ajoute à son contexte et applique avec les outils activés).
- Icône de robot remplacée par une étincelle.

## Vérifications locales requises avant release

- Fournir le wrapper Gradle et exécuter `:app:testDebugUnitTest`, `:app:lint` et `:app:assembleRelease`.
- Exécuter les tests `firestore.test.js` avec l'émulateur et les dépendances `@firebase/rules-unit-testing` / `firebase`.
- Tester sur appareil : démarrage à froid, onboarding, mode hors ligne, changement de thème, agent avec chaque capacité désactivée/activée, clés API erronées, URL non HTTPS, réponse HTTP d'erreur et reconnexion Google.
- Vérifier les secrets de signature Play, `versionCode`, icônes/adaptive icon, déclaration de confidentialité (transmission facultative de prompts et données de planning au fournisseur IA choisi), fiche Play Store, captures et consentements.
