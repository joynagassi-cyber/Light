#!/usr/bin/env bash
set -Eeuo pipefail
stage='validation des secrets'
trap 'echo "::error::Échec de la publication : $stage"' ERR
umask 077

for name in GOOGLE_SERVICES_JSON KEYSTORE_BASE64 STORE_PASSWORD KEY_ALIAS KEY_PASSWORD; do
  if [[ -z ${!name:-} ]]; then
    echo "::error::Secret de publication manquant : $name"
    exit 1
  fi
done

config=app/google-services.json
if [[ -e "$config" || -L "$config" ]]; then
  echo '::error::app/google-services.json existe déjà ; le fichier local ne sera pas remplacé.'
  exit 1
fi
export KEYSTORE_PATH
KEYSTORE_PATH=$(mktemp "${RUNNER_TEMP:-/tmp}/light-keystore.XXXXXXXX")
trap 'rm -f -- "$config" "$KEYSTORE_PATH"' EXIT

# Validate the configuration without echoing its contents or overwriting a local file.
stage='validation de la configuration Firebase'
python3 - "$config" <<'PY'
import base64
import json
import os
from pathlib import Path
import sys

try:
    raw = os.environ["GOOGLE_SERVICES_JSON"].strip()
    if not raw.startswith("{"):
        raw = base64.b64decode("".join(raw.split()), validate=True)
    payload = json.loads(raw)
    project_id = payload["project_info"]["project_id"]
    if not project_id or project_id.startswith("demo-"):
        raise ValueError
    client = next(c for c in payload["client"] if c["client_info"]["android_client_info"]["package_name"] == "com.aistudio.elanstudy.vkmrxz")
    if not client["client_info"]["mobilesdk_app_id"] or not any(k.get("current_key") for k in client["api_key"]):
        raise ValueError
    if not any(c.get("client_type") == 3 and c.get("client_id") for c in client["oauth_client"]):
        raise ValueError
except (KeyError, TypeError, ValueError, AttributeError, StopIteration):
    raise SystemExit("::error::GOOGLE_SERVICES_JSON invalide : projet Firebase, client Android ou OAuth Web manquant.") from None

with Path(sys.argv[1]).open("x") as config:
    json.dump(payload, config)
PY
stage='décodage du keystore'
printf '%s' "$KEYSTORE_BASE64" | base64 --decode > "$KEYSTORE_PATH"
unset GOOGLE_SERVICES_JSON KEYSTORE_BASE64

# Block both the fingerprint advertised in the old guide and the certificate
# actually embedded in the publicly exposed keystore.
stage='lecture du certificat de signature (keystore, alias ou mot de passe)'
fingerprint=$(keytool -list -v -J-Duser.language=en -J-Duser.country=US \
  -keystore "$KEYSTORE_PATH" -alias "$KEY_ALIAS" -storepass:env STORE_PASSWORD |
  awk '$1 == "SHA256:" {gsub(/:/, "", $2); if (!seen++) print tolower($2)}')
case "$fingerprint" in
  7201f852a9dae4091587d2fae631bdd82183d74c90050b14bfc9459216bc21be02|f8e1a3f55e7733e94e8e36a9ea977362cf380a0ef20dee7611ed83ab061b301e)
    echo '::error::Certificat de publication compromis. Utiliser une nouvelle clé privée.'
    exit 1
    ;;
esac
if [[ ! "$fingerprint" =~ ^[a-f0-9]{64}$ ]]; then
  echo '::error::Impossible de lire une empreinte SHA-256 valide du certificat.'
  exit 1
fi

stage='vérification de la clé privée (KEY_PASSWORD et KEY_ALIAS)'
keytool -certreq -keystore "$KEYSTORE_PATH" -alias "$KEY_ALIAS" \
  -storepass:env STORE_PASSWORD -keypass:env KEY_PASSWORD -file /dev/null

stage='assemblage Gradle de l’APK/AAB signé'
./gradlew :app:assembleRelease :app:bundleRelease -PlightSignedRelease=true --no-build-cache --console=plain
stage='vérification des signatures APK/AAB'
apk=app/build/outputs/apk/release/app-release.apk
aab=app/build/outputs/bundle/release/app-release.aab
actual=$("${ANDROID_HOME:?}/build-tools/36.0.0/apksigner" verify --print-certs "$apk" |
  awk -F ': ' '/^Signer #1 certificate SHA-256 digest:/ {print tolower($2)}')
[[ "$actual" == "$fingerprint" ]]
# Self-signed Android signing certificates are normal; verify integrity, not CA trust.
aab_fingerprint=$(keytool -printcert -jarfile "$aab" -J-Duser.language=en -J-Duser.country=US |
  awk '$1 == "SHA256:" {gsub(/:/, "", $2); if (!seen++) print tolower($2)}')
[[ "$aab_fingerprint" == "$fingerprint" ]]
LC_ALL=C jarsigner -J-Duser.language=en -verify "$aab" | grep -Fx 'jar verified.'

stage='préparation des fichiers de release'
mkdir -p release-output
cp "$apk" release-output/Light-release.apk
cp "$aab" release-output/Light-release.aab
(cd release-output && sha256sum Light-release.apk Light-release.aab > SHA256SUMS.txt)
