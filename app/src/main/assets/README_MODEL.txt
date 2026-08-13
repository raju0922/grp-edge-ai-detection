MODEL REQUIRED FOR REAL AI:
Place a compatible mobile object-detection model here as model.tflite.

The project currently uses StubObjectDetector so that the Android project remains
buildable without embedding a model whose input/output tensor specification is unknown.

After selecting a model, replace StubObjectDetector with a TensorFlow Lite/LiteRT
implementation matching that model's exact input and output tensors.
