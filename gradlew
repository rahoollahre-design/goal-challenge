#!/usr/bin/env sh
set -eu
VER=9.6.0
BOOT="${HOME}/.gradle/goalchallenge-bootstrap"
HOME_GRADLE="${BOOT}/gradle-${VER}"
ZIP="${BOOT}/gradle-${VER}-bin.zip"
if [ ! -x "${HOME_GRADLE}/bin/gradle" ]; then
  mkdir -p "${BOOT}"
  if command -v curl >/dev/null 2>&1; then
    curl -L "https://services.gradle.org/distributions/gradle-${VER}-bin.zip" -o "${ZIP}"
  else
    wget "https://services.gradle.org/distributions/gradle-${VER}-bin.zip" -O "${ZIP}"
  fi
  unzip -q -o "${ZIP}" -d "${BOOT}"
fi
exec "${HOME_GRADLE}/bin/gradle" "$@"
