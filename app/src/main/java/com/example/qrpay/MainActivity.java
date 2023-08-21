package com.example.qrpay;


import android.Manifest;
import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Point;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.mlkit.vision.barcode.BarcodeScanner;
import com.google.mlkit.vision.barcode.BarcodeScannerOptions;
import com.google.mlkit.vision.barcode.BarcodeScanning;
import com.google.mlkit.vision.barcode.common.Barcode;
import com.google.mlkit.vision.common.InputImage;

import java.util.List;

public class MainActivity extends AppCompatActivity {
    private MaterialButton CameraScan;
    private MaterialButton buttonGallery;
    private ImageView imageqr;
    private MaterialButton scanbtn;

    private static final int CAMERA_REQUEST_CODE = 100;
    private static final int STORAGE_REQUEST_CODE = 101;


    private String[] cameraPermissions;
    private String[] storagePermissions;

    private Uri imageUri = null;

    private BarcodeScannerOptions barcodeScannerOptions;
    private BarcodeScanner barcodeScanner;

    private static final String TAG ="MAIN_TAG";




    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        CameraScan =findViewById(R.id.CameraScan);
        buttonGallery = findViewById(R.id.buttonGallery);
        imageqr = findViewById(R.id.imageqr);
        scanbtn =findViewById(R.id.scanbtn);

        cameraPermissions = new String[] {Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};
        storagePermissions = new String[] {Manifest.permission.WRITE_EXTERNAL_STORAGE};


        barcodeScannerOptions = new BarcodeScannerOptions.Builder()
                        .setBarcodeFormats(Barcode.FORMAT_ALL_FORMATS)
                                .build();

        barcodeScanner = BarcodeScanning.getClient(barcodeScannerOptions);

        CameraScan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (checkCameraPermission()){
                    pickImageCamera();
                }
                else{
                    requestCameraPermission();
                }
            }
        });

        buttonGallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (checkStoragePermission()){

                    pickImageGallery();
                }
                else{

                  requestStoragePermission();
                }

            }
        });



        scanbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(imageUri==null){
                    Toast.makeText(MainActivity.this, "pick image first...", Toast.LENGTH_SHORT).show();
                }
                else{
                    detectResultFromImage();
                }
            }
        });




    }

    private void detectResultFromImage() {
        try{
            InputImage inputImage =InputImage.fromFilePath(this, imageUri);

            Task<List<Barcode>> barcodeResult = barcodeScanner.process(inputImage).addOnSuccessListener(new OnSuccessListener<List<Barcode>>() {
                @Override
                public void onSuccess(List<Barcode> barcodes) {

                    extractBarCodeQRCodeInfo(barcodes);



                }
            })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(MainActivity.this, "Scanning Failed due to" + e.getMessage(), Toast.LENGTH_SHORT).show();

                        }
                    });
        }
        catch (Exception e){
        Toast.makeText(this, "Failed due to" +e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void extractBarCodeQRCodeInfo(List<Barcode> barcodes) {

        for(Barcode barcode : barcodes){
            Rect bounds = barcode.getBoundingBox();
            Point[] corners = barcode.getCornerPoints();


            String rawValue = barcode.getRawValue();
            Log.d(TAG, "extractBarcoeQRCOdeInfo: rawValue: " + rawValue);

            int valueType = barcode.getValueType();
            switch(valueType){
                case Barcode.TYPE_WIFI:{
                    Barcode.WiFi typeWifi = barcode.getWifi();

                    String ssid = typeWifi.getSsid();
                    String password = "" + typeWifi.getPassword();
                    String encryptionType = "" + typeWifi.getEncryptionType();

                    Log.d(TAG, "extractBarcodeQRCodeInfo: ssid: " + ssid);
                    Log.d(TAG, "extractBarcodeQRCodeInfo: password:"+ password);
                    Log.d(TAG, "extractBarcodeQRCodeInfo: encryptionType: " + encryptionType);


                }
                break;
                case Barcode.TYPE_URL:{
                        Barcode.UrlBookmark typerUrl = barcode.getUrl();

                    String title = ""+ typerUrl.getTitle();
                    String url = ""+ typerUrl.getUrl();

                    Log.d(TAG, "extractBarcodeQRCodeInfo: TYPE_URL " );
                    Log.d(TAG, "extractBarcodeQRCodeInfo: title:"+ title);
                    Log.d(TAG, "extractBarcodeQRCodeInfo: encryptionType: " + url);
            }
            break;
                case Barcode.TYPE_EMAIL:{

                    Barcode.Email typeEmail = barcode.getEmail();

                    String address = "" + typeEmail.getAddress();
                    String body = "" + typeEmail.getBody();
                    String subject = "" + typeEmail.getSubject();


                    Log.d(TAG, "extractBarcodeQRCodeInfo: TYPE_EMAIL " );
                    Log.d(TAG, "extractBarcodeQRCodeInfo: address:"+ address);
                    Log.d(TAG, "extractBarcodeQRCodeInfo: body: " + body);
                    Log.d(TAG, "extractBarcodeQRCodeInfo: subject: " + subject);
                }
                break;
                




            }



        }
    }

    public void pickImageGallery(){

        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        galleryActivityResultLauncher.launch(intent);


    }

    private final ActivityResultLauncher<Intent> galleryActivityResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
        @Override
        public void onActivityResult(ActivityResult result) {

            if (result.getResultCode() == Activity.RESULT_OK){

                Intent data = result.getData();
                imageUri = data.getData();
                Log.d(TAG, "onActivityResult: imageUri: " +imageUri);
                imageqr.setImageURI(imageUri);
            }
            else{

                Toast.makeText(MainActivity.this, "Cancelled", Toast.LENGTH_SHORT).show();
            }

        }
    });


    private void pickImageCamera(){

        ContentValues contentValues = new ContentValues();
        contentValues.put(MediaStore.Images.Media.TITLE, "Sample Title");
        contentValues.put(MediaStore.Images.Media.DESCRIPTION, "Sample Image Description");

        imageUri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues);

        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE_SECURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
        cameraActivityResultLauncher.launch(intent);
    }

    private final ActivityResultLauncher<Intent> cameraActivityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {

                    if (result.getResultCode() == Activity.RESULT_OK){

                        Intent intent = result.getData();
                        Log.d(TAG, "onActivityResult: imageUri: "+ imageUri);
                        imageqr.setImageURI(imageUri);
                    }
                    else {

                        Toast.makeText(MainActivity.this, "Cancelled...", Toast.LENGTH_SHORT).show();
                    }
                    }

                }

    );

    private boolean checkStoragePermission() {

        boolean result = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;

        return result;
    }

    private void requestStoragePermission(){

        ActivityCompat.requestPermissions(this, storagePermissions, STORAGE_REQUEST_CODE);


    }

    private boolean checkCameraPermission(){

        boolean resultCamera = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED;

        boolean resultStorage = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;

        return resultCamera && resultStorage;
    }

    private void requestCameraPermission(){

        ActivityCompat.requestPermissions(this, cameraPermissions, CAMERA_REQUEST_CODE);


    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode) {
            case CAMERA_REQUEST_CODE: {

                if (grantResults.length > 0) {

                    boolean cameraAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    boolean storageAccepted = grantResults[1] == PackageManager.PERMISSION_GRANTED;

                    if (cameraAccepted && storageAccepted) {

                        pickImageCamera();
                    } else {
                        Toast.makeText(this, "Camera & Storage permissions are required...", Toast.LENGTH_SHORT).show();
                    }
                }

            }
            break;
            case STORAGE_REQUEST_CODE: {

                if (grantResults.length > 0) {

                    boolean storageAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;

                    if (storageAccepted) {

                        pickImageGallery();

                    }
                    else{
                        Toast.makeText(this, "Storage Permission required...", Toast.LENGTH_SHORT).show();
                    }

                }
                break;
            }
        }
    }
}