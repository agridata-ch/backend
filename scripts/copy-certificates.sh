#!/bin/sh
set -e

BASE_DIR=/certs

# Decodes a base64-encoded environment variable and writes it to a target file.
# Prints a warning and skips if the variable is not set.
decode_and_write() {
  TARGET_DIR=$1
  VAR_NAME=$2
  FILENAME=$3

  if [ -z "$(eval echo \$$VAR_NAME)" ]; then
    echo "WARNING: Environment variable '$VAR_NAME' is not set – skipping $TARGET_DIR/$FILENAME." >&2
    return
  fi

  mkdir -p "$TARGET_DIR"
  echo "$(eval echo \$$VAR_NAME)" | base64 -d > "$TARGET_DIR/$FILENAME"
  echo "Wrote $TARGET_DIR/$FILENAME"
}

# === AGIS certificate ===
decode_and_write "$BASE_DIR/agis" AGIS_CERTIFICATE_P12_BASE64 key-store.p12

# === BIT Signature certificate ===
decode_and_write "$BASE_DIR/bit-signature" BIT_SIGNATURE_CERTIFICATE_P12_BASE64 key-store.p12
decode_and_write "$BASE_DIR/bit-signature" BIT_SIGNATURE_TRUST_STORE_P12_BASE64 trust-store.p12
