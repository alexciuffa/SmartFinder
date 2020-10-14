package com.example.camera2_v04;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.io.FileNotFoundException;
import java.util.Arrays;
import java.util.Vector;

// for reference:
// https://www.tensorflow.org/lite/guide/inference#android_platform
// https://firebase.google.com/docs/ml/android/use-custom-models
public class ImagePreviewActivity extends AppCompatActivity {

    private ImageView faceView;
    private Button btnGenerateEncoding;
    private TextView encoddingTextView;

    Bitmap face_image, face_crop_image;
    FaceNet facenet_model;
    MTCNN mtcnn;

    Vector<Box> boxes;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_preview);

        faceView = (ImageView)findViewById(R.id.faceView);
        btnGenerateEncoding = (Button)findViewById(R.id.btnGenerateEncoding);
        encoddingTextView = (TextView)findViewById(R.id.encodingTextView);

        facenet_model = new FaceNet(getAssets());
        mtcnn = new MTCNN(getAssets());

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            String imagePath = extras.getString("Image_Location");
            int imageRotation = extras.getInt("Image_Rotation");
            //Toast.makeText(getApplicationContext(), Integer.toString(imageRotation), Toast.LENGTH_SHORT).show();
            try {
                //faceView.setRotation(get_rotation(imageRotation));
                face_image = RotateBitmap(BitmapFactory.decodeStream(getApplicationContext().openFileInput(imagePath)),
                        get_rotation(imageRotation));
                //faceView.setImageBitmap(face_image);
                draw_rectangle();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }

        btnGenerateEncoding.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (boxes.size() > 0) {
                    Toast.makeText(getApplicationContext(), "Searching for matches", Toast.LENGTH_SHORT).show();

                    int start_X = boxes.get(0).box[0];
                    int start_Y = boxes.get(0).box[1];
                    int yourwidth = boxes.get(0).box[2] - start_X;
                    int yourheight = boxes.get(0).box[3] - start_Y;

                    face_crop_image = Bitmap.createBitmap(face_image, start_X,start_Y,yourwidth, yourheight);

                    float[] model_output = facenet_model.get_embedding(face_crop_image);

                    //encoddingTextView.setText(Arrays.toString(model_output));

                    Intent intent = new Intent(getBaseContext(), MatchResultActivity.class);
                    intent.putExtra("vector_embedding", model_output);
                    startActivity(intent);

                } else {
                    Toast.makeText(getApplicationContext(), "No faces found", Toast.LENGTH_SHORT).show();
                }
            }
        });
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

    public static Bitmap RotateBitmap(Bitmap source, float angle) {
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(), matrix, true);
    }

    public void draw_rectangle(){
        Bitmap bm= Utils.copyBitmap(face_image);
        try {
            boxes = mtcnn.detectFaces(bm,40);
            if (boxes.size() > 0) {
                Utils.drawRect(bm,boxes.get(0).transform2Rect());
                Utils.drawPoints(bm,boxes.get(0).landmark);
            } else {
                Toast.makeText(getApplicationContext(), "No faces found", Toast.LENGTH_SHORT).show();
            }
            //for (int i=0;i<boxes.size();i++){
            //    Utils.drawRect(bm,boxes.get(i).transform2Rect());
            //    Utils.drawPoints(bm,boxes.get(i).landmark);
            //}
            faceView.setImageBitmap(bm);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

}