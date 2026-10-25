#!/usr/bin/env bash
set -Eeuo pipefail

PROJECT_DIR="$(cd -- "$(dirname -- "${BASH_SOURCE[0]}")" && pwd)"
BUILD_SCRIPT="$PROJECT_DIR/build-and-install.sh"
[[ -x "$BUILD_SCRIPT" ]] || { echo "Missing executable: $BUILD_SCRIPT" >&2; exit 1; }
command -v gsettings >/dev/null 2>&1 || {
    echo "gsettings was not found. This shortcut installer supports Ubuntu GNOME." >&2
    exit 1
}

BASE="/org/gnome/settings-daemon/plugins/media-keys"
CUSTOM_KEYBINDINGS="$BASE/custom-keybindings"
BINDING_NAME="goal-challenge-build"
BINDING_PATH="$CUSTOM_KEYBINDINGS/$BINDING_NAME/"
KEYBINDING_LIST_KEY="custom-keybindings"

gsettings list-schemas | grep -qx 'org.gnome.settings-daemon.plugins.media-keys' || {
    echo "GNOME media-keys settings are unavailable in this desktop session." >&2
    exit 1
}
gsettings list-relocatable-schemas | grep -qx 'org.gnome.settings-daemon.plugins.media-keys.custom-keybinding' || {
    echo "GNOME custom-keybinding schema is unavailable." >&2
    exit 1
}

CURRENT="$(gsettings get org.gnome.settings-daemon.plugins.media-keys custom-keybindings)"
python3 - "$CURRENT" "$BINDING_PATH" <<'PY' > /tmp/goal-challenge-keybindings.txt
import ast, sys
items = ast.literal_eval(sys.argv[1])
path = sys.argv[2]
if path not in items:
    items.append(path)
print(repr(items))
PY
NEW_LIST="$(cat /tmp/goal-challenge-keybindings.txt)"
rm -f /tmp/goal-challenge-keybindings.txt

gsettings set org.gnome.settings-daemon.plugins.media-keys custom-keybindings "$NEW_LIST"
gsettings set "org.gnome.settings-daemon.plugins.media-keys.custom-keybinding:$BINDING_PATH" name 'Goal Challenge: Build, Install & Launch'
gsettings set "org.gnome.settings-daemon.plugins.media-keys.custom-keybinding:$BINDING_PATH" command "/bin/bash \"$BUILD_SCRIPT\""
gsettings set "org.gnome.settings-daemon.plugins.media-keys.custom-keybinding:$BINDING_PATH" binding '<Control><Alt>b'

printf '\nKeyboard shortcut configured: Ctrl+Alt+B\n'
printf 'It runs: %s\n' "$BUILD_SCRIPT"
printf 'Connect and unlock one Android phone, enable USB debugging, and accept the authorization prompt before using it.\n'
