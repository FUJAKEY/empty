#!/usr/bin/env bash
set -euo pipefail

BASE64_PATH="gradle/wrapper/gradle-wrapper.jar.base64"
JAR_PATH="gradle/wrapper/gradle-wrapper.jar"

if [[ ! -f "$BASE64_PATH" ]]; then
  echo "Missing $BASE64_PATH; cannot restore Gradle wrapper." >&2
  exit 1
fi

mkdir -p "$(dirname "$JAR_PATH")"
base64 -d "$BASE64_PATH" > "$JAR_PATH"

if [[ ! -s "$JAR_PATH" ]]; then
  echo "Restored wrapper JAR is empty; check $BASE64_PATH." >&2
  exit 1
fi

echo "Gradle wrapper restored to $JAR_PATH"
