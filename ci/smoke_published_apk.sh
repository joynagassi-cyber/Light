#!/usr/bin/env bash
# Test the immutable, public v1.0.0 artifact on a fresh Android emulator.
set -euo pipefail

mode=${1:?Pass verify or install}
folder=${2:?Pass the evidence directory}
apk="$folder/Light-release.apk"
package='com.aistudio.elanstudy.vkmrxz'
sha='5eea2619bfca1ae62383139c673576a416d145b9156fde31bf4e3a638bf5330b'
# SHA-256 of the signing certificate used for Light's first public release.
cert='d167b2c477b3a0f3222b26a0a1818acfd3ea4f9a3f233a603b68532d76f82a1e'

verify() {
    local tools="${ANDROID_HOME:?}/build-tools/36.0.0"
    (
        cd "$folder"
        sha256sum --check --ignore-missing SHA256SUMS.txt
        printf '%s  Light-release.apk\n' "$sha" | sha256sum --check -
        wc -c Light-release.apk
    )
    [[ $(wc -c < "$apk") -eq 83761102 ]]
    signature=$("$tools/apksigner" verify --verbose --print-certs --min-sdk-version 26 "$apk")
    printf '%s\n' "$signature" | grep -E '^Verifies$|^Verified using v[0-9]|^Signer #1 certificate SHA-256 digest:'
    grep -Fxq "Signer #1 certificate SHA-256 digest: $cert" <<< "$signature"
    badging=$("$tools/aapt" dump badging "$apk")
    grep -E '^package:|^sdkVersion:|^targetSdkVersion:|^native-code:' <<< "$badging"
    grep -Fq "package: name='$package' versionCode='1' versionName='1.0.0'" <<< "$badging"
    grep -Fxq "sdkVersion:'26'" <<< "$badging"
    sed -n "s/^launchable-activity: name='\([^']*\)'.*/\1/p" <<< "$badging" > "$folder/activity"
    [[ -s "$folder/activity" ]]
}

install() {
    local requested_api=${1:?Pass the expected Android API level}
    local reported_api installed launch activity pid initial_pid
    reported_api=$(adb shell getprop ro.build.version.sdk | tr -d '\r')
    printf 'API réelle : %s\nABI : %s\n' "$reported_api" "$(adb shell getprop ro.product.cpu.abilist | tr -d '\r')"
    [[ $reported_api == "$requested_api" ]]
    adb shell pm list packages "$package" | tr -d '\r' > "$folder/previous-package"
    if grep -Fxq "package:$package" "$folder/previous-package"; then
        echo 'First install test requires a clean emulator.' >&2
        return 1
    fi
    python3 ci/android_install.py "$apk" --checksums "$folder/SHA256SUMS.txt"
    installed=$(adb shell pm path "$package" | tr -d '\r')
    [[ $installed == package:* ]]
    echo 'Package installé (Package Manager).'
    activity=$(cat "$folder/activity")
    launch=$(adb shell am start -W -n "$package/$activity" | tr -d '\r')
    printf '%s\n' "$launch"
    grep -Fxq 'Status: ok' <<< "$launch"
    initial_pid=$(adb shell pidof "$package" | tr -d '\r')
    [[ -n $initial_pid ]]
    sleep 8
    pid=$(adb shell pidof "$package" | tr -d '\r')
    [[ $pid == "$initial_pid" ]]
    echo 'L’activité a démarré et le même processus est encore actif après 8 secondes.'
}

case "$mode" in
  verify) verify 2>&1 | tee "$folder/verification.txt.log" ;;
  install) install "${3:?Pass API level}" 2>&1 | tee "$folder/installation.txt.log" ;;
  *) printf 'Unknown mode: %s\n' "$mode" >&2; exit 2 ;;
esac
