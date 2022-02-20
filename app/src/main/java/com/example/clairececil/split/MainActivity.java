package com.example.clairececil.split;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresPermission;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MainActivity extends AppCompatActivity {

    Uri image;
    private static final int CAMERA_REQUEST = 1888;
    private static final int GET_FROM_GALLERY = 3;
    File photoFile = null;

    String mCurrentPhotoPath = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == CAMERA_REQUEST && resultCode == RESULT_OK) {

            String path = photoFile.getAbsolutePath();
            Intent i = new Intent(this, ReadImage.class);
            i.putExtra("imageURI", path);
            startActivity(i);
        } else if(requestCode==GET_FROM_GALLERY && resultCode == Activity.RESULT_OK) {
            Uri selectedImage = data.getData();
            Intent i = new Intent(this, ReadImage.class);
            String path = selectedImage.toString();
            i.putExtra("imageURI", path);
            startActivity(i);
        } else {
            Toast.makeText(getBaseContext(), "Request cancelled or something went wrong", Toast.LENGTH_LONG).show();
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == 0) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED
                    && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                    // Create the File where the photo should go
                    try {

                        photoFile = createImageFile();
                        Toast.makeText(getBaseContext(), photoFile.getAbsolutePath(), Toast.LENGTH_LONG).show();
                        Log.i("Absolute Path", photoFile.getAbsolutePath());

                        // Continue only if the File was successfully created
                        if (photoFile != null) {
                            Uri photoURI = FileProvider.getUriForFile(this,
                                    "com.example.clairececil.split.fileprovider",
                                    photoFile);
                            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                            startActivityForResult(takePictureIntent, CAMERA_REQUEST);
                        }
                    } catch (Exception ex) {
                        // Error occurred while creating the File
                        Toast.makeText(getBaseContext(), ex.getMessage().toString(), Toast.LENGTH_LONG).show();
                    }


                }else
                {
                    Toast.makeText(getBaseContext(), "null", Toast.LENGTH_LONG).show();
                }
            } else {
                Toast.makeText(getBaseContext(), "App won't work without camera permissions", Toast.LENGTH_LONG).show();
            }
        }

    }

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        mCurrentPhotoPath = image.getAbsolutePath();
        return image;
    }

    public void onTakePhoto(View v) {
        takePhoto();
    }

    public void onUploadImage(View v) {
        startActivityForResult(new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.INTERNAL_CONTENT_URI), GET_FROM_GALLERY);
    }

    public void takePhoto() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[] { Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE }, 0);
        } else {
            Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                // Create the File where the photo should go
                try {

                    photoFile = createImageFile();
                    Toast.makeText(getBaseContext(), photoFile.getAbsolutePath(), Toast.LENGTH_LONG).show();
                    Log.i("Split", photoFile.getAbsolutePath());

                    // Continue only if the File was successfully created
                    if (photoFile != null) {
                        Uri photoURI = FileProvider.getUriForFile(this,
                                "com.example.clairececil.split.fileprovider",
                                photoFile);
                        takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                        System.out.println("******************************");
                        System.out.println(photoFile.getAbsolutePath());
                        System.out.println("******************************");
                        startActivityForResult(takePictureIntent, CAMERA_REQUEST);
                    }
                } catch (Exception ex) {
                    // Error occurred while creating the File
                    Toast.makeText(getBaseContext(), ex.getMessage().toString(), Toast.LENGTH_LONG).show();
                }


            }else
            {
                Toast.makeText(getBaseContext(), "null", Toast.LENGTH_LONG).show();
            }
        }
    }
}
