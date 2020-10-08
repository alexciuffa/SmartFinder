package com.example.camera2_v04;

import androidx.appcompat.app.AppCompatActivity;

import android.content.res.AssetFileDescriptor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import org.tensorflow.lite.Interpreter;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.Arrays;

// for reference:
// https://www.tensorflow.org/lite/guide/inference#android_platform
// https://firebase.google.com/docs/ml/android/use-custom-models
public class ImagePreviewActivity extends AppCompatActivity {

    private ImageView faceView;
    private Button btnGenerateEncoding;
    private TextView encodingTextView;

    Interpreter tflite_model;
    Bitmap face_image;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_preview);

        faceView = (ImageView)findViewById(R.id.faceView);
        btnGenerateEncoding = (Button)findViewById(R.id.btnGenerateEncoding);
        encodingTextView = (TextView)findViewById(R.id.encodingTextView);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            String imagePath = extras.getString("Image_Location");
            int imageRotation = extras.getInt("Image_Rotation");
            //Toast.makeText(getApplicationContext(), Integer.toString(imageRotation), Toast.LENGTH_SHORT).show();
            try {
                faceView.setRotation(get_rotation(imageRotation));
                face_image = BitmapFactory.decodeStream(getApplicationContext().openFileInput(imagePath));
                faceView.setImageBitmap(face_image);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }

        btnGenerateEncoding.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Load model
                tflite_model = load_tflite_model("facenet.tflite");

                // Make inference
                ByteBuffer model_input = pre_process_image(face_image, 160, 160);
                float[] model_output = inference(model_input, tflite_model);

                // Set output to textView
                encodingTextView.setText(Arrays.toString(model_output));

                // Close model
                tflite_model.close();
            }
        });
    }

    private MappedByteBuffer loadModelFile(String modelFileName) throws IOException {
        AssetFileDescriptor fileDescriptor = this.getAssets().openFd(modelFileName);
        FileInputStream fileInputStream = new FileInputStream(fileDescriptor.getFileDescriptor());
        FileChannel fileChannel = fileInputStream.getChannel();
        long startOffsets = fileDescriptor.getStartOffset();
        long declaredLength = fileDescriptor.getDeclaredLength();

        return fileChannel.map(FileChannel.MapMode.READ_ONLY,startOffsets,declaredLength);
    }

    private Interpreter load_tflite_model(String modelFileName) {
        try {
            return new Interpreter(loadModelFile(modelFileName));
        } catch (IllegalArgumentException | IOException e){
            e.printStackTrace();
        }
        return null;
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

    private float[] inference(ByteBuffer myModel_input, Interpreter myModel) {
        float[][] outputValue = new float[1][128];
        myModel.run(myModel_input, outputValue);
        return outputValue[0];

        /*Then, allocate a ByteBuffer large enough to contain the model's output and pass the input
          buffer and output buffer to the TensorFlow Lite interpreter's run() method. For example,
          for an output shape of [1 1000] floating-point values:
         */
        //int bufferSize = 1000 * java.lang.Float.SIZE / java.lang.Byte.SIZE;
        //ByteBuffer modelOutput = ByteBuffer.allocateDirect(bufferSize).order(ByteOrder.nativeOrder());
        //interpreter.run(input, modelOutput);
    }

    private float get_rotation(int imageRotation) {
        if(imageRotation == 90) {
            return 90;
        }
        if(imageRotation == 180) {
            return 0;
        }
        if(imageRotation == 0) {
            return 180;
        }
        return -90;
    };



}