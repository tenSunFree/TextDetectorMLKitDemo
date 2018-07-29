package com.example.administrator.textdetectormlkitdemo;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.text.FirebaseVisionText;
import com.google.firebase.ml.vision.text.FirebaseVisionTextDetector;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    static final int REQUEST_IMAGE_CAPTURE = 1;

    private Button snapBtn, detectBtn;
    private ImageView imageView;
    private TextView txtView;
    private Bitmap imageBitmap;
    private StringBuffer stringBuffer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        snapBtn = findViewById(R.id.snapBtn);
        detectBtn = findViewById(R.id.detectBtn);
        imageView = findViewById(R.id.imageView);
        txtView = findViewById(R.id.txtView);

        snapBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                /** 拍照 */
                dispatchTakePictureIntent();
            }
        });

        detectBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                /** 開始文字識別 */
                detectTxt();
            }
        });
    }

    /** 透過Intent去開啟Android的照相機介面, 然後拍完照後, 即呼叫onActivityResult() */
    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);                 // 利用intent去開啟Android的照相機介面

        /** 判定所要启动的Activity是否存在 */
        if (takePictureIntent.resolveActivityInfo(
                getPackageManager(), PackageManager.MATCH_DEFAULT_ONLY) != null) {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        } else {
            Toast.makeText(this, "找不到指定的Activity", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        /** 取得照片的縮圖, 並賦予imageView */
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            Bundle extras = data.getExtras();
            imageBitmap = (Bitmap) extras.get("data");                                              // 取到照片的縮圖
            imageView.setImageBitmap(imageBitmap);
        }
    }

    /** 將照片縮圖的bitmap 進行文字識別 */
    private void detectTxt() {
        if (imageBitmap != null) {
            FirebaseVisionImage image = FirebaseVisionImage.fromBitmap(imageBitmap);                 // 从Bitmap对象创建FirebaseVisionImage对象
            FirebaseVisionTextDetector detector = FirebaseVision.getInstance().getVisionTextDetector();
            detector.detectInImage(image).addOnSuccessListener(new OnSuccessListener<FirebaseVisionText>() {
                @Override
                public void onSuccess(FirebaseVisionText firebaseVisionText) {
                    processTxt(firebaseVisionText);
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                }
            });
        }
    }

    /** 將識別後取得的FirebaseVisionText 轉成text, 再賦予txtView */
    private void processTxt(FirebaseVisionText text) {
        List<FirebaseVisionText.Block> blocks = text.getBlocks();
        stringBuffer = new StringBuffer();
        if (blocks.size() == 0) {
            Toast.makeText(MainActivity.this, "No Text ...", Toast.LENGTH_SHORT).show();
            return;
        }
        for (FirebaseVisionText.Block block : text.getBlocks()) {
            stringBuffer.append(block.getText() + "\n");
            txtView.setText(stringBuffer);
        }
    }
}
