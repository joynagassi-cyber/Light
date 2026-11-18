# 🔐 Coffre-Fort des Secrets & Clés de Signature — Application Light

Ce document regroupe toutes les clés de signature, empreintes cryptographiques pour Google Cloud / Firebase, et les valeurs exactes prêtes à copier-coller dans vos **GitHub Secrets**.

---

## 📌 1. Tableau récapitulatif des GitHub Secrets à renseigner

Rendez-vous sur **GitHub** > **Votre Dépôt (`Light`)** > **Settings** > **Secrets and variables** > **Actions** > **New repository secret** :

| Nom exact du Secret GitHub | Valeur à renseigner | Description |
| :--- | :--- | :--- |
| **`GOOGLE_SERVICES_JSON`** | *(Voir Section 2 ci-dessous pour le JSON complet)* | Configuration Firebase & OAuth Google Sign-In |
| **`KEYSTORE_BASE64`** | *(Voir Section 3 ci-dessous pour la chaîne Base64)* | Keystore de signature Release encodé en Base64 |
| **`STORE_PASSWORD`** | `Light_App_Release_2026_KeySecure!` | Mot de passe de protection du Keystore |
| **`KEY_ALIAS`** | `upload` | Alias de la clé privée de signature |
| **`KEY_PASSWORD`** | `Light_App_Release_2026_KeySecure!` | Mot de passe de la clé privée |
| **`ENV_FILE`** *(Optionnel)* | *(Voir Section 5 ci-dessous)* | Clés d'API optionnelles pour les fournisseurs IA |

---

## 🔥 2. Valeur exacte de `GOOGLE_SERVICES_JSON` (à copier dans GitHub Secrets)

Copiez l'intégralité du bloc JSON ci-dessous et collez-le dans le secret **`GOOGLE_SERVICES_JSON`** :

```json
{
  "project_info": {
    "project_number": "531903108775",
    "project_id": "gen-lang-client-0206081112",
    "storage_bucket": "gen-lang-client-0206081112.firebasestorage.app"
  },
  "client": [
    {
      "client_info": {
        "mobilesdk_app_id": "1:531903108775:android:2429667f59e5f8fbde6d82",
        "android_client_info": {
          "package_name": "com.aistudio.elanstudy.vkmrxz"
        }
      },
      "oauth_client": [
        {
          "client_id": "531903108775-j9k7e1d9l4ks536vg89v21oll6rmrudc.apps.googleusercontent.com",
          "client_type": 1,
          "android_info": {
            "package_name": "com.aistudio.elanstudy.vkmrxz",
            "certificate_hash": "50be0ce080a2eb937e7920f29b3a138b5e357cfd"
          }
        },
        {
          "client_id": "531903108775-mp8s6l8bv0avis35l25o1en89kcg35ki.apps.googleusercontent.com",
          "client_type": 3
        }
      ],
      "api_key": [
        {
          "current_key": "AIzaSyBKP5cwBpfJ888shn1a0ZI-VXAD49f9dSI"
        }
      ],
      "services": {
        "appinvite_service": {
          "other_platform_oauth_client": [
            {
              "client_id": "531903108775-mp8s6l8bv0avis35l25o1en89kcg35ki.apps.googleusercontent.com",
              "client_type": 3
            }
          ]
        }
      }
    }
  ],
  "configuration_version": "1"
}
```

---

## 🔑 3. Keystore de Signature Android (Prêt à l'emploi)

### Caractéristiques de la clé :
- **Fichier Keystore :** `my-upload-key.jks` (Format standard PKCS12 compatible Java 11/17/21 & Android)
- **Alias :** `upload`
- **Algorithme :** RSA 2048 bits
- **Validité :** 10 000 jours (~27 ans)
- **Mot de passe Keystore & Clé :** `Light_App_Release_2026_KeySecure!`

### Valeur exacte de `KEYSTORE_BASE64` (à copier dans GitHub Secrets) :

```text
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
```

---

## 🛡️ 4. Empreintes de Certificat Enregistrées

Ces empreintes correspondent à votre configuration :

- **Package Name :** `com.aistudio.elanstudy.vkmrxz`
- **Application Name :** `Light`
- **Base Firestore :** `ai-studio-android-lan-cb9bb915-7486-4610-a057-db58c7e693a7`
- **Project Number :** `531903108775`
- **Project ID :** `gen-lang-client-0206081112`

### Empreintes :
```text
SHA-1:   50:BE:0C:E0:80:A2:EB:93:7E:79:20:F2:9B:3A:13:8B:5E:35:7C:FD
SHA-256: 72:01:F8:52:A9:DA:E4:09:15:87:D2:FA:E6:31:BD:D8:21:83:D7:4D:00:50:14:BF:C9:45:92:16:BC:21:BE:02
```

---

## 🤖 5. Clés optionnelles des Fournisseurs d'IA (`ENV_FILE`)

Si vous utilisez des clés d'API personnalisées pour l'assistant :

```env
ANTHROPIC_API_KEY=sk-ant-api03-...
OPENAI_API_KEY=sk-proj-...
GROQ_API_KEY=gsk_...
OPENROUTER_API_KEY=sk-or-v1-...
```

---

## 🚀 6. Déclenchement & Téléchargement des Builds

- Dès que vous poussez sur `main` ou ouvrez une Pull Request, le workflow valide automatiquement les règles de sécurité Firestore, le linter et les tests unitaires.
- Rendez-vous dans **GitHub Actions** > **Light CI/CD Pipeline** > **Run workflow** pour lancer la génération manuelle.
- À la fin de l'exécution, téléchargez votre APK signé **`Light-release.apk`** ou l'App Bundle **`Light-release.aab`** directement dans la section **Artifacts**.
