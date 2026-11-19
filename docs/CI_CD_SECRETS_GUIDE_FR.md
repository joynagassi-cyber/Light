# CI/CD Android de Light

## Workflows GitHub Actions

La validation est définie dans `.github/workflows/android.yml` et la publication
manuelle dans `.github/workflows/release.yml`.

**Light Android CI** exécute `assembleDebug`, `assembleRelease` et
`testDebugUnitTest` sur les PR et sur `main`, avec JDK 17 et le SDK Android 36.1.
Elle ne lit aucun secret. L’APK debug est un artefact de test en mode local,
compatible avec Android 8.0 ou ultérieur ; ce n’est pas une version publique.
Les tests Android nécessitant un émulateur Firebase ne s’exécutent que si cet
émulateur est disponible. Les tests de règles Firestore (`npm run test:rules`)
ne font pas partie de ce workflow Android.

## Préparer la signature et Firebase

N’utilisez pas l’ancienne clé de signature, [compromise par sa publication](../LIGHT_SECRETS_AND_KEYSTORE.md).
Générez et sauvegardez la nouvelle clé hors du dépôt :

```sh
keytool -genkeypair -keystore light-release.jks -alias upload -keyalg RSA -keysize 2048 -validity 10000
keytool -list -v -keystore light-release.jks -alias upload
```

Ajoutez les nouvelles empreintes SHA-1 et SHA-256 à l’application Android
`com.aistudio.elanstudy.vkmrxz` dans Firebase. Activez le fournisseur Google
et téléchargez `google-services.json` après configuration. Vérifiez aussi les
règles de la base Firestore configurée dans l’application et les paramètres
App Check / Play Integrity pour le canal de distribution voulu.

Créez l’environnement **`android-release`** dans **Settings → Environments**.
Limitez-le à la branche `main` et ajoutez les secrets suivants à cet environnement :

| Secret | Valeur |
| --- | --- |
| `KEYSTORE_BASE64` | Contenu base64 de la nouvelle clé (`base64 < light-release.jks \| tr -d '\n'`) |
| `STORE_PASSWORD` | Mot de passe du keystore |
| `KEY_ALIAS` | Alias de la clé privée (par exemple `upload`) |
| `KEY_PASSWORD` | Mot de passe de la clé privée |
| `GOOGLE_SERVICES_JSON` | Configuration Firebase réelle (JSON ou base64) |

N’utilisez ni le mode debug ni une clé temporaire pour une release publique.
Ne placez pas de clés d’API privées dans un `.env` utilisé pour compiler un APK :
les valeurs intégrées au binaire sont extractibles.

## Publier la première release

1. Après la fusion et le passage au vert de la CI de `main`, ouvrez
   **Actions → Light Android Release → Run workflow** sur `main`.
2. Saisissez `1.0.0`, la valeur actuelle de `versionName`. Pour les versions
   suivantes, mettez d’abord à jour `versionName` et augmentez `versionCode`.
3. La CD relance la validation avec le commit exact, puis assemble et vérifie
   les fichiers signés. Le job de publication crée `v1.0.0` sur ce même commit
   avec `Light-release.apk`, `Light-release.aab` et `SHA256SUMS.txt`. Il ne
   remplace pas un tag existant.
4. Vérifiez le résultat dans **Releases** et installez l’APK sur un appareil
   pour tester le démarrage et, si configurée, la connexion Google.

Si un secret manque, est invalide ou correspond à l’ancienne clé, la CD s’arrête
avant publication. Elle ne fabrique ni configuration Firebase ni clé de secours.
