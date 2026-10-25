#!/usr/bin/env bash
set -Eeuo pipefail

PROJECT_DIR="$(cd -- "$(dirname -- "${BASH_SOURCE[0]}")" && pwd)"
APK_PATH="$PROJECT_DIR/app/build/outputs/apk/debug/app-debug.apk"
PACKAGE_NAME="com.goalchallenge.game.debug"
MAIN_ACTIVITY="com.goalchallenge.game.ui.MainActivity"

pause_before_exit() {
    local status=$?
    if [[ -t 0 ]]; then
        printf '\n'
        read -r -p "Press Enter to close this window..." || true
    fi
    exit "$status"
}
trap pause_before_exit EXIT

fail() {
    printf 'ERROR: %s\n' "$*" >&2
    exit 1
}

main() {
    cd "$PROJECT_DIR"

    local adb_bin
    if command -v adb >/dev/null 2>&1; then
        adb_bin="$(command -v adb)"
    else
        adb_bin="${ANDROID_HOME:-$HOME/Android/Sdk}/platform-tools/adb"
    fi
    [[ -x "$adb_bin" ]] || fail "ADB not found. Check Android SDK platform-tools setup."

    printf 'Checking connected Android device...\n'
    local device_output
    device_output="$("$adb_bin" devices)" || fail "Could not communicate with ADB. Try: adb kill-server && adb start-server"
    local -a devices=()
    mapfile -t devices < <(printf '%s\n' "$device_output" | awk 'NR > 1 && $2 == "device" { print $1 }')

    if [[ ${#devices[@]} -ne 1 ]]; then
        printf '%s\n' "$device_output"
        fail "Expected exactly one authorized, online device; found ${#devices[@]}. Unlock the phone, reconnect USB, and allow USB debugging."
    fi
    local serial="${devices[0]}"

    printf '\nBuilding the game...\n'
    ./gradlew :game-core:test :app:assembleDebug

    [[ -f "$APK_PATH" ]] || fail "Build succeeded but APK was not found at $APK_PATH"

    printf '\nInstalling on %s...\n' "$serial"
    "$adb_bin" -s "$serial" install -r -t "$APK_PATH"

    printf '\nOpening Goal Challenge...\n'
    "$adb_bin" -s "$serial" shell am start -n "$PACKAGE_NAME/$MAIN_ACTIVITY"
    printf '\nDone. Goal Challenge is installed and open on your phone.\n'
}

main "$@"
