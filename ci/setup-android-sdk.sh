#!/usr/bin/env bash
set -euo pipefail

sdk=${ANDROID_HOME:?ANDROID_HOME must point to an Android SDK}
platform='platforms;android-36.1'
build_tools='build-tools;36.0.0'
packages=()
[[ -f "$sdk/${platform/;/\/}/android.jar" ]] || packages+=("$platform")
[[ -x "$sdk/${build_tools/;/\/}/apksigner" ]] || packages+=("$build_tools")
if ((${#packages[@]} == 0)); then
  echo 'Android SDK 36.1 and Build Tools 36.0.0 are already installed.'
  exit 0
fi

# GitHub-hosted runners can have the SDK on disk without sdkmanager on PATH.
manager=$(command -v sdkmanager || true)
if [[ -z "$manager" ]]; then
  for candidate in "$sdk"/cmdline-tools/latest/bin/sdkmanager "$sdk"/cmdline-tools/*/bin/sdkmanager; do
    if [[ -x "$candidate" ]]; then
      manager=$candidate
      break
    fi
  done
fi
if [[ -z "$manager" ]]; then
  echo '::error::Android command-line tools (sdkmanager) are missing.'
  exit 1
fi
"$manager" --sdk_root="$sdk" "${packages[@]}"
