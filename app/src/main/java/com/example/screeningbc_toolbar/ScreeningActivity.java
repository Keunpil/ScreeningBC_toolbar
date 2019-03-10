package com.example.screeningbc_toolbar;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfFloat;
import org.opencv.core.MatOfInt;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

public class ScreeningActivity extends AppCompatActivity {

    private final int SELECT_PHOTO = 1;
    private ImageView ivImage;
    private Bitmap selectedImage;
    private Bitmap currentBitmap;
    private int temp;
    private Mat originalMat, src_gray;
    private Mat grayMat = new Mat();
    private Mat cannyEdges = new Mat();
    private Mat hierarchy = new Mat();
    static int ACTION_PICK_PHOTO = 1;
    public final static int PICK_PHOTO_CODE = 1046;

    // A list to store all array of the contours
    List<MatOfPoint> contourList = new ArrayList<MatOfPoint>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_screening);
        Toolbar toolbar = findViewById(R.id.toolbar_screening);
        setSupportActionBar(toolbar);

        ivImage = (ImageView)findViewById(R.id.imageView_screening);

        Button btnSelect = findViewById(R.id.btn_select_picture);

        btnSelect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
                photoPickerIntent.setType("image/*");
                startActivityForResult(photoPickerIntent, ACTION_PICK_PHOTO);
            }
        });

        Button btnScreening = findViewById(R.id.bnt_screening);

        btnScreening.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (selectedImage != null) {

                    //Converting the image to grayscale
                    Imgproc.cvtColor(originalMat, grayMat, Imgproc.COLOR_BGR2GRAY);

                    Imgproc.Canny(grayMat, cannyEdges, 10, 100);

                    Imgproc.findContours(cannyEdges, contourList, hierarchy, Imgproc.RETR_LIST, Imgproc.CHAIN_APPROX_SIMPLE);

                    //Drawing contours on a new image
                    Mat contours = new Mat();
                    contours.create(cannyEdges.rows(), cannyEdges.cols(), CvType.CV_8UC3);
                    Random r = new Random();
                    for (int i = 0; i < contourList.size(); i++) {
                        Imgproc.drawContours(contours, contourList, i , new Scalar(r.nextInt(255), r.nextInt(255), r.nextInt(255)), -1);
                    }

                    // Converting Mat back to Bitmap
                    Utils.matToBitmap(contours, currentBitmap);
                    ivImage.setImageBitmap(currentBitmap);
                } else {
                    Toast.makeText(getApplicationContext(), "사진을 먼저 선택하세요", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent imageReturnedIntent) {
        super.onActivityResult(requestCode, resultCode, imageReturnedIntent);

        switch(requestCode) {
            case SELECT_PHOTO:
                if(resultCode == RESULT_OK) {
                    try {
                        // Code to load image into a Bitmap and convert it to a Mat for processing.
                        final Uri imageUri = imageReturnedIntent.getData();
                        final InputStream imageStream = getContentResolver().openInputStream(imageUri);

                        selectedImage = BitmapFactory.decodeStream(imageStream);

                        //Change bitmap to Mat
                        Bitmap tempBitmap = selectedImage.copy(Bitmap.Config.ARGB_8888, true);
                        originalMat = new Mat(tempBitmap.getHeight(), tempBitmap.getWidth(), CvType.CV_8U);
                        Utils.bitmapToMat(tempBitmap, originalMat);
                        currentBitmap = selectedImage.copy(Bitmap.Config.ARGB_8888, false);

                        ivImage.setImageBitmap(selectedImage);  // Display image in ImageView

                        //src = new Mat(selectedImage.getHeight(), selectedImage.getWidth(), CvType.CV_8UC4);
                        //Utils.bitmapToMat(selectedImage, src);
                        //src_gray = new Mat(selectedImage.getHeight(), selectedImage.getWidth(), CvType.CV_8UC1);

                        //processedImage = Bitmap.createScaledBitmap(selectedImage, 500, 450, true);
                        //Log.i("imageType", CvType.typeToString(src.type()) + "");
                        //Utils.matToBitmap(src, processedImage);
                        //ivImage.setImageBitmap(processedImage);
                        Log.i("image", "image load done");
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                    break;
                }
        }
    }

}
