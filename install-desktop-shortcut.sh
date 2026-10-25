#!/usr/bin/env bash
set -Eeuo pipefail

PROJECT_DIR="$(cd -- "$(dirname -- "${BASH_SOURCE[0]}")" && pwd)"

if command -v xdg-user-dir >/dev/null 2>&1; then
    DESKTOP_DIR="$(xdg-user-dir DESKTOP)"
fi
DESKTOP_DIR="${DESKTOP_DIR:-$HOME/Desktop}"
LAUNCHER_PATH="$DESKTOP_DIR/Goal Challenge - Build and Install.desktop"

mkdir -p "$DESKTOP_DIR"
cat > "$LAUNCHER_PATH" <<EOF
[Desktop Entry]
Version=1.0
Type=Application
Name=Goal Challenge - Build and Install
Comment=Build Goal Challenge and install it on the connected Android phone
Exec=/bin/bash "$PROJECT_DIR/build-and-install.sh"
Path="$PROJECT_DIR"
Icon=utilities-terminal
Terminal=true
StartupNotify=false
Categories=Development;
EOF
chmod +x "$LAUNCHER_PATH"

printf 'Desktop shortcut installed: %s\n' "$LAUNCHER_PATH"
printf 'If prompted, choose "Allow Launching" before double-clicking it.\n'
