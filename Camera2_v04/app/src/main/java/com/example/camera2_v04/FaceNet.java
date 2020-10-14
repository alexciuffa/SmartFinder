package com.example.camera2_v04;

import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.Color;

import org.tensorflow.lite.Interpreter;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;

public class FaceNet {

    private static final String MODEL_FILE  = "facenet.tflite";
    private Interpreter facenet_interpreter;
    private AssetManager assetManager;

    FaceNet(AssetManager mgr){
        assetManager=mgr;
        load_tflite_model();
    }

    private MappedByteBuffer loadModelFile(String modelFileName) throws IOException {
        AssetFileDescriptor fileDescriptor = assetManager.openFd(modelFileName);
        FileInputStream fileInputStream = new FileInputStream(fileDescriptor.getFileDescriptor());
        FileChannel fileChannel = fileInputStream.getChannel();
        long startOffsets = fileDescriptor.getStartOffset();
        long declaredLength = fileDescriptor.getDeclaredLength();

        return fileChannel.map(FileChannel.MapMode.READ_ONLY,startOffsets,declaredLength);
    }

    private void load_tflite_model() {
        try {
            facenet_interpreter = new Interpreter(loadModelFile(MODEL_FILE));
        } catch (IllegalArgumentException | IOException e){
            e.printStackTrace();
            //return false;
        }
        //return true;
    }

    private ByteBuffer pre_process_image(Bitmap inputImage, int width, int height) {
        /*
        Assuming the tflite model has an input shape of [1  224 3] floating-point values, this function
        pre-process an image with to get an input of the right shape for your model by generating an
        input ByteBuffer from a Bitmap object.

        Input: Bitmap inputImage,
        Output: ByteBuffer input
         */
        Bitmap bitmap = Bitmap.createScaledBitmap(inputImage, width, height, true);
        ByteBuffer input = ByteBuffer.allocateDirect(width * height * 3 * 4).order(ByteOrder.nativeOrder());
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int px = bitmap.getPixel(x, y);

                // Get channel values from the pixel value.
                int r = Color.red(px);
                int g = Color.green(px);
                int b = Color.blue(px);

                // Normalize channel values to [-1.0, 1.0]. This requirement depends
                // on the model. For example, some models might require values to be
                // normalized to the range [0.0, 1.0] instead.
                float rf = (r - 127) / 255.0f;
                float gf = (g - 127) / 255.0f;
                float bf = (b - 127) / 255.0f;

                input.putFloat(rf);
                input.putFloat(gf);
                input.putFloat(bf);
            }
        }
        return input;
    }

    private float[] inference(ByteBuffer myModel_input) {
        float[][] outputValue = new float[1][512];
        facenet_interpreter.run(myModel_input, outputValue);
        return outputValue[0];

        /*Then, allocate a ByteBuffer large enough to contain the model's output and pass the input
          buffer and output buffer to the TensorFlow Lite interpreter's run() method. For example,
          for an output shape of [1 1000] floating-point values:
         */
        //int bufferSize = 1000 * java.lang.Float.SIZE / java.lang.Byte.SIZE;
        //ByteBuffer modelOutput = ByteBuffer.allocateDirect(bufferSize).order(ByteOrder.nativeOrder());
        //interpreter.run(input, modelOutput);
    }

    public float[] get_embedding(Bitmap face_image){
        // Make inference
        ByteBuffer model_input = pre_process_image(face_image, 160, 160);
        float[] model_output = inference(model_input);
        return model_output;
    }

    public void close_model() {
        facenet_interpreter.close();
    }

}
