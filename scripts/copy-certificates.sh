#!/bin/sh
set -e

BASE_DIR=/certs

# Ensures that a required environment variable is set; exits if missing
require_env() {
  VAR_NAME=$1
  if [ -z "$(eval echo \$$VAR_NAME)" ]; then
    echo "ERROR: Environment variable '$VAR_NAME' is not set." >&2
    exit 1
  fi
}

# Decodes a base64-encoded environment variable and writes it to a target file
decode_and_write() {
  TARGET_DIR=$1
  VAR_NAME=$2
  FILENAME=$3

  require_env "$VAR_NAME"

  mkdir -p "$TARGET_DIR"
  echo "$(eval echo \$$VAR_NAME)" | base64 -d > "$TARGET_DIR/$FILENAME"
  echo "Wrote $TARGET_DIR/$FILENAME"
}

# === AGIS certificate ===
decode_and_write "$BASE_DIR/agis" AGIS_CERTIFICATE_P12_BASE64 key-store.p12
