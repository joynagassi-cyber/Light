# Guide CI/CD & Configuration des Secrets GitHub (Light)

Ce guide détaille le fonctionnement du pipeline d'Intégration et Déploiement Continus (**CI/CD**) pour l'application **Light**, ainsi que les instructions pas-à-pas pour générer les clés, configurer Google & Firebase, et renseigner les **GitHub Secrets**.

---

## 1. Noms exacts des Secrets GitHub à renseigner

Pour configurer vos secrets dans GitHub :
1. Rendez-vous sur votre dépôt GitHub.
2. Allez dans **Settings** > **Secrets and variables** > **Actions**.
3. Cliquez sur **New repository secret** pour chaque variable ci-dessous.

| Nom exact du Secret | Description | Obligatoire pour Release |
| :--- | :--- | :---: |
| `KEYSTORE_BASE64` | Contenu encodé en base64 de votre fichier Keystore (`.jks` ou `.keystore`) | **Oui** |
| `STORE_PASSWORD` | Mot de passe du Keystore | **Oui** |
| `KEY_ALIAS` | Alias de la clé privée de signature (ex: `upload`) | **Oui** |
| `KEY_PASSWORD` | Mot de passe de la clé privée (généralement identique à `STORE_PASSWORD`) | **Oui** |
| `GOOGLE_SERVICES_JSON` | Contenu textuel complet du fichier `google-services.json` de Firebase | **Oui** |
| `ENV_FILE` | Contenu de votre fichier `.env` (clés d'API AI providers : Groq, OpenAI, Anthropic, etc.) | Optionnel |

---

## 2. Procédure détaillée : Signature Android & Keystore

### Étape 2.1 : Générer la clé de signature (Keystore)
Exécutez la commande suivante dans votre terminal local :

```bash
keytool -genkeypair -v \
  -keystore my-upload-key.jks \
  -alias upload \
  -keyalg RSA \
  -keysize 2048 \
  -validity 10000
```
> ⚠️ **Conservez précieusement ce fichier `my-upload-key.jks` et vos mots de passe !** Si vous le perdez, vous ne pourrez plus mettre à jour votre application sur le Google Play Store.

### Étape 2.2 : Récupérer les empreintes SHA-1 et SHA-256 (pour Google / Firebase)
Pour autoriser la connexion Google (Google Sign-In) et App Check (Play Integrity), vous devez extraire les empreintes :

```bash
keytool -list -v -keystore my-upload-key.jks -alias upload
```
Notez :
- **SHA-1** (ex: `AB:CD:12:34:...`)
- **SHA-256** (ex: `12:34:56:78:...`)

### Étape 2.3 : Convertir le fichier Keystore en chaîne Base64

- **Sur Linux / macOS :**
  ```bash
  base64 -w 0 my-upload-key.jks
  # Ou sur macOS si -w n'est pas supporté :
  base64 -i my-upload-key.jks
  ```

- **Sur Windows (PowerShell) :**
  ```powershell
  [Convert]::ToBase64String([IO.File]::ReadAllBytes("my-upload-key.jks")) | Set-Clipboard
  ```

Copiez le résultat obtenu et collez-le dans le secret GitHub nommé **`KEYSTORE_BASE64`**.

---

## 3. Configuration Google Cloud & Firebase

### Étape 3.1 : Paramétrer l'application Android dans Firebase Console
1. Rendez-vous sur la [Console Firebase](https://console.firebase.google.com/).
2. Ouvrez votre projet Firebase.
3. Allez dans les **Paramètres du projet** (icône d'engrenage en haut à gauche) > onglet **Général**.
4. Dans la section **Vos applications**, sélectionnez votre application Android ou cliquez sur **Ajouter une application**.
5. Renseignez les paramètres suivants :
   - **Nom de package Android (Package Name) :** `com.aistudio.elanstudy.vkmrxz`
   - **Surnom de l'application :** `Light`
   - **Empreinte de certificat de signature SHA-1 :** Collez le SHA-1 généré à l'étape 2.2.
   - **Empreinte SHA-256 :** Collez également le SHA-256.
6. Cliquez sur **Enregistrer** et téléchargez le fichier **`google-services.json`**.

### Étape 3.2 : Intégrer `google-services.json` dans GitHub Secrets
1. Ouvrez le fichier `google-services.json` téléchargé dans un éditeur de texte (VS Code, Bloc-notes, etc.).
2. Copiez l'intégralité du texte (JSON).
3. Créez le secret GitHub nommé **`GOOGLE_SERVICES_JSON`** et collez-y ce contenu.

### Étape 3.3 : Activer Firebase Authentication (Google Sign-In)
1. Dans la console Firebase, menu de gauche > **Authentication** > onglet **Sign-in method**.
2. Activez le fournisseur **Google** et enregistrez.
3. Le plugin Google Services génère automatiquement la chaîne `default_web_client_id` utilisée par l'application pour le composant `CredentialManager`.

### Étape 3.4 : Configurer la base Cloud Firestore
- L'application pointe sur la base de données Firestore :
  `ai-studio-android-lan-cb9bb915-7486-4610-a057-db58c7e693a7`
- Assurez-vous que cette base existe dans votre projet Firebase et que les règles `firestore.rules` y sont appliquées.

### Étape 3.5 : Activer App Check & Play Integrity
1. Dans Firebase, allez dans **App Check**.
2. Enregistrez votre application Android avec **Play Integrity**.

---

## 4. Configuration des variables d'environnement (`ENV_FILE`)

Si vous utilisez des clés pour les fournisseurs IA (OpenAI, Anthropic, Groq, OpenRouter), vous pouvez créer un secret **`ENV_FILE`** avec le contenu suivant :

```env
# Clés API pour les assistants d'étude (optionnel)
ANTHROPIC_API_KEY=sk-ant-api03-...
OPENAI_API_KEY=sk-proj-...
GROQ_API_KEY=gsk_...
OPENROUTER_API_KEY=sk-or-v1-...
```

---

## 5. Fonctionnement du Pipeline CI/CD (`.github/workflows/ci-cd.yml`)

Le workflow s'exécute automatiquement lors de :
- Tout **Push** sur la branche `main`
- Tout **Pull Request** visant la branche `main`
- Tout **Tag Git** au format `v*` (ex: `v1.0.0`)
- Déclenchement manuel via l'onglet **Actions** de GitHub (**Run workflow**)

### Jobs exécutés :

1. **Job 1 : Tests & Non-Régression (`test-and-validate`)**
   - Mise en place de l'environnement **JDK 17** et **Node.js 20**.
   - Démarrage de l'émulateur Firebase local.
   - Exécution des 7 suites de tests de sécurité Firestore (`firestore.test.js`).
   - Analyse statique du code Kotlin/Android avec **Android Lint** (`./gradlew lintDebug`).
   - Exécution de l'ensemble des tests unitaires et d'interfaces **Robolectric / JUnit** (`./gradlew testDebugUnitTest`).
   - Archivage automatique des rapports de test et d'analyse.

2. **Job 2 : Build APK Release & Bundle (`build-release-apk`)**
   - Ne s'exécute que si les tests du Job 1 ont tous réussi (**garantie anti-régression**).
   - Décodage automatique et sécurisé du `google-services.json` et de la clé Keystore.
   - Compilation et signature de l'**APK Release** (`Light-release.apk`) et de l'**Android App Bundle** (`Light-release.aab`).
   - Téléversement direct des fichiers dans les **Artefacts GitHub** (téléchargeables en un clic depuis l'interface GitHub Actions).
   - Création automatique d'une **GitHub Release** avec l'APK et l'AAB en pièce jointe lors de la création d'un tag `v*`.
