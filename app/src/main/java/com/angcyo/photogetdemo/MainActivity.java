package com.angcyo.photogetdemo;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {

    ImageView imageView;
    PopupMenu phoneGetMenu;
    FloatingActionButton fab;
    Bitmap imageSelect;
    Uri imageUri;
    boolean isCrop = false;

    /**
     * scale image
     *
     * @param org
     * @param newWidth
     * @param newHeight
     * @return
     */
    public static Bitmap scaleImageTo(Bitmap org, int newWidth, int newHeight) {
        float scaleWidth = (float) newWidth / org.getWidth();
        float scaleHeight = (float) newHeight / org.getHeight();
        float scale = Math.max(scaleWidth, scaleHeight);
        return scaleImage(org, scale, scale);
    }

    /**
     * scale image
     *
     * @param org
     * @param scaleWidth  sacle of width
     * @param scaleHeight scale of height
     * @return
     */
    public static Bitmap scaleImage(Bitmap org, float scaleWidth, float scaleHeight) {
        if (org == null) {
            return null;
        }

        Matrix matrix = new Matrix();
        matrix.postScale(scaleWidth, scaleHeight);
        return Bitmap.createBitmap(org, 0, 0, org.getWidth(), org.getHeight(), matrix, true);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                        .setAction("Action", null).show();
                phoneGetMenu.show();
            }
        });

        initViews();
        initEvents();
    }

    private void initEvents() {
        phoneGetMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.get_photo:
                        getPhotoFromPhotos();
                        break;
                    case R.id.get_camera:
                        getPhotoFromCamera();
                        break;
                    case R.id.crop_photo:
                        cropPhotoFromPhotos();
                        break;
                    case R.id.crop_camera:
                        cropPhotoFromCamera();
                        break;
                }
                return true;
            }
        });
    }

    public void getPhotoFromCamera() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.setAction(MediaStore.ACTION_IMAGE_CAPTURE);
        imageUri = Uri.fromFile(new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM),
                UUID.randomUUID() + ".jpg"));
        intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri); // set the image file name
        startActivityForResult(intent, 110);
    }

    public void getPhotoFromPhotos() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "选择图片"), 100);
    }

    public void cropPhotoFromCamera() {
        isCrop = true;
        getPhotoFromCamera();
    }

    public void cropPhotoFromPhotos() {
        isCrop = true;
        getPhotoFromPhotos();
    }

    /**
     * 用给定的uri,调用 裁剪程序
     */
    private void cropImageByUri(Uri uri, int outputX, int outputY, int requestCode) {
        Intent intent = new Intent("com.android.camera.action.CROP");
        intent.setDataAndType(uri, "image/*");
        intent.putExtra("crop", "true");
        intent.putExtra("aspectX", 2);
        intent.putExtra("aspectY", 1);
        intent.putExtra("outputX", outputX);
        intent.putExtra("outputY", outputY);
        intent.putExtra("scale", true);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
        intent.putExtra("return-data", false);
        intent.putExtra("outputFormat", Bitmap.CompressFormat.JPEG.toString());
        intent.putExtra("noFaceDetection", true); // no face detection
        startActivityForResult(intent, requestCode);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == 100 && data != null) {
            imageUri = data.getData();
            if (imageUri != null) {//相册选择
                if (isCrop) {
                    cropImageByUri(imageUri, 800, 480, 130);
                    isCrop = false;
                    return;
                }
                try {
                    setImageView(imageUri);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {//拍照选择,不能设置MediaStore.EXTRA_OUTPUT
                Bundle extras = data.getExtras();
                if (extras != null) {
                    try {
                        setImageView(((Bitmap) extras.getParcelable("data")));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        } else if (resultCode == RESULT_OK && requestCode == 110 && imageUri != null) {//拍照选择,设置MediaStore.EXTRA_OUTPUT
            if (isCrop) {
                cropImageByUri(imageUri, 800, 480, 130);
                isCrop = false;
                return;
            }
            try {
                setImageView(imageUri);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else if (resultCode == RESULT_OK && requestCode == 130 && imageUri != null) {//拍照选择,设置MediaStore.EXTRA_OUTPUT
            try {
                setImageView(imageUri);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void setImageView(Uri imageUri) throws IOException {
        if (imageSelect != null) {
            imageSelect.recycle();
        }
        imageSelect = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageUri);
        if (imageSelect != null) {
            imageView.setImageBitmap(scaleImageTo(imageSelect, imageView.getWidth(), imageView.getHeight()));
        }
    }

    private void setImageView(Bitmap bitmap) throws IOException {
        if (imageSelect != null) {
            imageSelect.recycle();
        }
        imageSelect = bitmap;
        if (imageSelect != null) {
            imageView.setImageBitmap(scaleImageTo(imageSelect, imageView.getWidth(), imageView.getHeight()));
        }
    }

    private void initViews() {
        imageView = (ImageView) findViewById(R.id.imageView);
        phoneGetMenu = new PopupMenu(this, fab);
        phoneGetMenu.inflate(R.menu.menu_phone_get);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
