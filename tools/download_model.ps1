$url = "https://storage.googleapis.com/mediapipe-models/object_detector/efficientdet_lite0/int8/1/efficientdet_lite0.tflite"
$out = Join-Path $PSScriptRoot "..\app\src\main\assets\efficientdet_lite0.tflite"
New-Item -ItemType Directory -Force (Split-Path $out) | Out-Null
Invoke-WebRequest -Uri $url -OutFile $out
Write-Host "Model downloaded to $out"
