# EdgeGuard — AI Object Detection Integrated

This version connects the EdgeGuard pipeline to a real on-device object detector.

## AI stack

- CameraX for camera frames
- MediaPipe Tasks Vision ObjectDetector
- EfficientDet-Lite0 INT8 model
- Java implementation
- On-device inference
- Object tracking
- Direction estimation
- Risk scoring
- Text-to-Speech
- Vibration alerts

Google's MediaPipe Object Detector supports Android live/video/image modes and accepts
a compatible TFLite model. The official documentation also shows using CameraX
ImageProxy frames via `MediaImageBuilder`. See:
https://ai.google.dev/edge/mediapipe/solutions/vision/object_detector/android

## Model

The project expects:

`app/src/main/assets/efficientdet_lite0.tflite`

Official Google model URL:

https://storage.googleapis.com/mediapipe-models/object_detector/efficientdet_lite0/int8/1/efficientdet_lite0.tflite

The model is about 4.6 MB. Download it once and copy it into the assets folder.

## Why the model is not embedded in this ZIP

The execution environment cannot attach the binary model from Google's storage
endpoint directly into the generated ZIP. The source code is already wired for it;
only the binary model file must be placed in the specified assets directory.

## What happens after adding the model

Camera frame
 -> MediaPipe MPImage
 -> EfficientDet-Lite0
 -> detections
 -> ObjectTracker
 -> RiskEngine
 -> voice/vibration alert

The model can detect common COCO objects such as people, cars, bicycles, buses,
trucks, dogs, chairs, bottles and many others. Detection quality depends on lighting,
camera quality, distance and model confidence.

## Important

This is an object detector, not yet a collision-avoidance system. The RiskEngine in
this project is a research/demo heuristic based on object class, position, bounding
box size and temporal growth. It must not be presented as a safety-certified system.

## Build steps

1. Open this folder in Android Studio.
2. Allow Gradle sync.
3. Download `efficientdet_lite0.tflite` from the official URL above.
4. Put it at `app/src/main/assets/efficientdet_lite0.tflite`.
5. Build and install on a physical Android phone.
6. Grant Camera and Microphone permissions.
7. Press START ASSISTANCE.
8. Point the rear camera at common objects.

