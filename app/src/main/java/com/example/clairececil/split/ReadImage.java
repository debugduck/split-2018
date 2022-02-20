/**The code for the camera is based on
 * Mayank Sanghvi's YouTube tutorial
 * and blog for how to save a full sized
 * image with the camera in Android
 * Video can be found here:
 * https://www.youtube.com/watch?v=-W3qpuYr3lk
 * Original code can be found here:
 * https://vlemon.com/blog/android/android-capture-image-from-camera-and-get-image-save-path/
 */
package com.example.clairececil.split;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.media.Image;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseArray;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.text.TextBlock;
import com.google.android.gms.vision.text.TextRecognizer;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.regex.Pattern;

public class ReadImage extends AppCompatActivity {

    ImageView receiptImage;
    Bitmap bitmap;
    Uri photoURI;
    ArrayList<Double> prices;
    ArrayList<String> items;

    Uri image;
    private static final int CAMERA_REQUEST = 1888;
    File photoFile = null;

    String mCurrentPhotoPath = "";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_read_image);

        receiptImage = (ImageView) findViewById(R.id.image_receipt);

        if(getIntent().hasExtra("imageURI")) {
            photoURI = Uri.parse(getIntent().getStringExtra("imageURI"));
            receiptImage.setImageURI(photoURI);
            receiptImage.setVisibility(View.VISIBLE);
        }

        new ReadImageTask().execute();
    }

    public void onNext(View v) {
        Intent i = new Intent(this, GroupInfo.class);
        i.putStringArrayListExtra("items", items);
        i.putExtra("prices", prices);
        startActivity(i);
    }

    private class ReadImageTask extends AsyncTask<Void, Void, String> {
        protected String doInBackground(Void... voids) {
            String result = "there's no image for some reason";

            //ImageView test = (ImageView) findViewById(R.id.imageView9);

            bitmap = ((BitmapDrawable) receiptImage.getDrawable()).getBitmap();

            if(bitmap != null) {
                TextRecognizer textRecognizer = new TextRecognizer.Builder(getApplicationContext()).build();

                if(!textRecognizer.isOperational()) {
                    Log.w("ReadImage", "Something went wrong my dude");
                } else {
                    Frame frame = new Frame.Builder().setBitmap(bitmap).build();

                    SparseArray<TextBlock> items = textRecognizer.detect(frame);

                    // Regex for a decimal number
                    // "^\\d*\\.\\d+|\\d+\\.\\d*$"
                    StringBuilder sb = new StringBuilder();
                    for(int i = 0; i < items.size(); ++i) {
                        TextBlock item = items.valueAt(i);
                        sb.append(item.getValue());
                        sb.append("\n");
                    }

                    result = sb.toString();
                }
            }
            return result;
        }

        protected void onPostExecute(String result) {

            TextView item_list = (TextView) findViewById(R.id.items_text);
            TextView price_list = (TextView) findViewById(R.id.prices_text);

            prices = getPrices(result);
            items = getItems(result);

            price_list.append("\n");
            for(Double price : prices) {
                price_list.append(price + "\n");
            }

            item_list.append("\n");
            for(String item : items) {
                item_list.append(item + "\n");
            }

            item_list.append("Subtotal\n");
            item_list.append("Tax\n");
            item_list.append("Total\n");
        }

        private ArrayList<Double> getPrices(String result) {
            ArrayList<Double> prices = new ArrayList<>();

            String[] lines = result.split("\n");
            for(String line : lines) {
                line = line.trim();
                System.out.println("Current line: \"" + line + "\"");
                line = removeExtraChars(line);
                if(line.matches("\\d+\\.\\d+([eE]\\d+)?")) {
                    prices.add(Double.parseDouble(line.trim()));
                }
            }
            return prices;
        }

        private ArrayList<String> getItems(String result) {
            ArrayList<String> items = new ArrayList<>();

            String[] lines = result.split("\n");
            for(String line : lines) {
                line = line.trim();
                System.out.println("Current line: \"" + line + "\"");
                String number = "[0-9]+";
                if(line.length() >= 2) {
                    String check1 = String.valueOf(line.charAt(0));
                    String check2 = String.valueOf(line.charAt(0)) + String.valueOf(line.charAt(1));


                    if ((check1.matches(number) && hasLetters(line)) ||
                            (check2.matches(number) && hasLetters(line))) {
                        items.add(line);
                    }
                }
            }
            return items;
        }

        private String removeExtraChars(String line) {
            StringBuilder sb = new StringBuilder();
            for(int i = 0; i < line.length(); i++) {
                if(String.valueOf(line.charAt(i)).matches("[0-9]+") || line.charAt(i) == '.') {
                    sb.append(line.charAt(i));
                }
            }
            return sb.toString();
        }

        private boolean hasLetters(String line) {
            for(int i = 0; i < line.length(); i++) {
                if(Character.isLetter(line.charAt(i))) {
                    return true;
                }
            }
            return false;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == CAMERA_REQUEST && resultCode == RESULT_OK) {
            String path = photoFile.getAbsolutePath();
            photoURI = Uri.parse(path);
            receiptImage.setImageURI(photoURI);
            receiptImage.setVisibility(View.VISIBLE);
            TextView item_list = (TextView) findViewById(R.id.items_text);
            TextView price_list = (TextView) findViewById(R.id.prices_text);
            item_list.setText(null);
            price_list.setText(null);
            item_list.setText("Items: \n");
            price_list.setText("Prices: \n");
            new ReadImageTask().execute();
        }
        else
        {
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

    public void onRetry(View v) {
        takePhoto();
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
