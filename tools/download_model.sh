#!/usr/bin/env bash
set -e
URL="https://storage.googleapis.com/mediapipe-models/object_detector/efficientdet_lite0/int8/1/efficientdet_lite0.tflite"
OUT="$(cd "$(dirname "$0")/.." && pwd)/app/src/main/assets/efficientdet_lite0.tflite"
mkdir -p "$(dirname "$OUT")"
curl -L "$URL" -o "$OUT"
echo "Model downloaded to $OUT"
