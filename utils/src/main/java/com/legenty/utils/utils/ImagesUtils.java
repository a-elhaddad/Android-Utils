package com.legenty.utils.utils;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.util.Base64;
import android.util.Log;

import com.legenty.utils.R;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import static android.content.ContentValues.TAG;

@SuppressLint("SdCardPath")
public class ImagesUtils {


    Context context;
    private Activity currentActivity;
    private Fragment currentFragment;

    private ImageAttachmentListener imageAttachmentCallBack;

    private String selectedPath = "";
    private Uri imageUri;
    private File path = null;

    private int from = 0;
    private boolean isFragment = false;

    private static final int CAMERA_REQUEST_AFTER = 1888;
    private static final int CAMERA_REQUEST_SERIAL_NUMBER = 1887;
    private static final int CAMERA_REQUEST_CONTARCT = 1886;

    public ImagesUtils(Activity act) {

        this.context = act;
        this.currentActivity = act;
        imageAttachmentCallBack = (ImageAttachmentListener) context;
    }

    public ImagesUtils(Activity act, Fragment fragment, boolean isFragment) {

        this.context = act;
        this.currentActivity = act;
        imageAttachmentCallBack = (ImageAttachmentListener) fragment;
        if (isFragment) {
            this.isFragment = true;
            currentFragment = fragment;
        }

    }

    public String getfilenameFromPath(String path) {
        return path.substring(path.lastIndexOf('/') + 1, path.length());

    }

    public Uri getImageUri(Context context, Bitmap photo) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        photo.compress(Bitmap.CompressFormat.PNG, 80, bytes);
        String path = MediaStore.Images.Media.insertImage(context.getContentResolver(), photo, "Title", null);
        return Uri.parse(path);
    }

    public String getPath(Uri uri) {
        String[] projection = {MediaStore.Images.Media.DATA};
        Cursor cursor = this.context.getContentResolver().query(uri, projection, null, null, null);
        int column_index = 0;
        if (cursor != null) {
            column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            String path = cursor.getString(column_index);
            cursor.close();
            return path;
        } else
            return uri.getPath();
    }

    public Bitmap StringToBitMap(String encodedString) {
        try {
            byte[] encodeByte = Base64.decode(encodedString, Base64.DEFAULT);
            Bitmap bitmap = BitmapFactory.decodeByteArray(encodeByte, 0, encodeByte.length);
            return bitmap;
        } catch (Exception e) {
            e.getMessage();
            return null;
        }
    }

    public String BitMapToString(Bitmap bitmap) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 80, baos);
        byte[] b = baos.toByteArray();
        String temp = Base64.encodeToString(b, Base64.DEFAULT);
        return temp;
    }

    public boolean isDeviceSupportCamera() {
        if (this.context.getPackageManager().hasSystemFeature(
                PackageManager.FEATURE_CAMERA)) {
            // this device has a camera
            return true;
        } else {
            // no camera on this device
            return false;
        }
    }

    public Bitmap compressImage(String imageUri, float height, float width) {

        String filePath = getRealPathFromURI(imageUri);
        Bitmap scaledBitmap = null;

        BitmapFactory.Options options = new BitmapFactory.Options();

        // by setting this field as true, the actual bitmap pixels are not loaded in the memory. Just the bounds are loaded. If
        // you try the use the bitmap here, you will get null.

        options.inJustDecodeBounds = true;
        Bitmap bmp = BitmapFactory.decodeFile(filePath, options);

        int actualHeight = options.outHeight;
        int actualWidth = options.outWidth;

        // max Height and width values of the compressed image is taken as 816x612

        float maxHeight = height;
        float maxWidth = width;
        float imgRatio = actualWidth / actualHeight;
        float maxRatio = maxWidth / maxHeight;

        // width and height values are set maintaining the aspect ratio of the image

        if (actualHeight > maxHeight || actualWidth > maxWidth) {
            if (imgRatio < maxRatio) {
                imgRatio = maxHeight / actualHeight;
                actualWidth = (int) (imgRatio * actualWidth);
                actualHeight = (int) maxHeight;
            } else if (imgRatio > maxRatio) {
                imgRatio = maxWidth / actualWidth;
                actualHeight = (int) (imgRatio * actualHeight);
                actualWidth = (int) maxWidth;
            } else {
                actualHeight = (int) maxHeight;
                actualWidth = (int) maxWidth;

            }
        }

        //  setting inSampleSize value allows to load a scaled down version of the original image

        options.inSampleSize = calculateInSampleSize(options, actualWidth, actualHeight);

        //  inJustDecodeBounds set to false to load the actual bitmap
        options.inJustDecodeBounds = false;

        // this options allow android to claim the bitmap memory if it runs low on memory

        options.inPurgeable = true;
        options.inInputShareable = true;
        options.inTempStorage = new byte[16 * 1024];

        try {
            //  load the bitmap from its path
            bmp = BitmapFactory.decodeFile(filePath, options);
        } catch (OutOfMemoryError exception) {
            exception.printStackTrace();

        }
        try {
            scaledBitmap = Bitmap.createBitmap(actualWidth, actualHeight, Bitmap.Config.ARGB_8888);
        } catch (OutOfMemoryError exception) {
            exception.printStackTrace();
        }

        float ratioX = actualWidth / (float) options.outWidth;
        float ratioY = actualHeight / (float) options.outHeight;
        float middleX = actualWidth / 2.0f;
        float middleY = actualHeight / 2.0f;

        Matrix scaleMatrix = new Matrix();
        scaleMatrix.setScale(ratioX, ratioY, middleX, middleY);

        Canvas canvas = new Canvas(scaledBitmap);
        canvas.setMatrix(scaleMatrix);
        canvas.drawBitmap(bmp, middleX - bmp.getWidth() / 2, middleY - bmp.getHeight() / 2, new Paint(Paint.FILTER_BITMAP_FLAG));

        ExifInterface exif;
        try {
            exif = new ExifInterface(filePath);

            int orientation = exif.getAttributeInt(
                    ExifInterface.TAG_ORIENTATION, 0);
            Log.d("EXIF", "Exif: " + orientation);
            Matrix matrix = new Matrix();
            if (orientation == 6) {
                matrix.postRotate(90);
                Log.d("EXIF", "Exif: " + orientation);
            } else if (orientation == 3) {
                matrix.postRotate(180);
                Log.d("EXIF", "Exif: " + orientation);
            } else if (orientation == 8) {
                matrix.postRotate(270);
                Log.d("EXIF", "Exif: " + orientation);
            }
            scaledBitmap = Bitmap.createBitmap(scaledBitmap, 0, 0, scaledBitmap.getWidth(), scaledBitmap.getHeight(), matrix,
                    true);

            return scaledBitmap;
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    private String getRealPathFromURI(String contentURI) {
        Uri contentUri = Uri.parse(contentURI);
        Cursor cursor = context.getContentResolver().query(contentUri, null, null, null, null);
        if (cursor == null) {
            return contentUri.getPath();
        } else {
            cursor.moveToFirst();
            int index = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
            return cursor.getString(index);
        }
    }

    public int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {
            final int heightRatio = Math.round((float) height / (float) reqHeight);
            final int widthRatio = Math.round((float) width / (float) reqWidth);
            inSampleSize = heightRatio < widthRatio ? heightRatio : widthRatio;
        }
        final float totalPixels = width * height;
        final float totalReqPixelsCap = reqWidth * reqHeight * 2;
        while (totalPixels / (inSampleSize * inSampleSize) > totalReqPixelsCap) {
            inSampleSize++;
        }

        return inSampleSize;
    }

    public void launchCamera(int from) {
        this.from = from;

        if (Build.VERSION.SDK_INT >= 23) {
            if (isFragment)
                permissionCheckFragment(1);
            else
                permissionCheck(1);
        } else {
            cameraCall();
        }
    }


    public void launchGallery(int from) {

        this.from = from;

        if (Build.VERSION.SDK_INT >= 23) {
            if (isFragment)
                permissionCheckFragment(2);
            else
                permissionCheck(2);
        } else {
            galleyCall();
        }
    }


    public void imagePicker(final int from) {
        this.from = from;

        final CharSequence[] items;

        if (from == CAMERA_REQUEST_AFTER || from == CAMERA_REQUEST_SERIAL_NUMBER || from == CAMERA_REQUEST_CONTARCT) {

            items = new CharSequence[1];
            items[0] = currentActivity.getString(R.string.take_photo_camera);
        } else {
            if (isDeviceSupportCamera()) {
                items = new CharSequence[2];
                items[0] = currentActivity.getString(R.string.take_photo_camera);
                items[1] = currentActivity.getString(R.string.take_photo_gallery);
            } else {
                items = new CharSequence[1];
                items[0] = currentActivity.getString(R.string.take_photo_gallery);
            }
        }


        AlertDialog.Builder alertdialog = new AlertDialog.Builder(currentActivity);
        alertdialog.setTitle(currentActivity.getString(R.string.take_photo));
        alertdialog.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int item) {
                if (items[item].equals(currentActivity.getString(R.string.take_photo_camera))) {
                    launchCamera(from);
                } else if (items[item].equals(currentActivity.getString(R.string.take_photo_gallery))) {
                    launchGallery(from);
                }
            }
        });
        alertdialog.show();
    }

    public void permissionCheck(final int code) {
        String[] neededPermissions = new String[]{
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.CAMERA};

        if (Build.VERSION.SDK_INT >= 23) {
            if (ContextCompat.checkSelfPermission(currentActivity, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED &&
                    ContextCompat.checkSelfPermission(currentActivity, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                Log.d(TAG, "permissionCheckFragment: " + code);
                if (code == 1)
                    cameraCall();
                else if (code == 2)
                    galleyCall();
            } else {

                ActivityCompat.requestPermissions(currentActivity, neededPermissions, 0);
                return;

            }
        }
    }

    public void permissionCheckFragment(final int code) {

        String[] neededPermissions = new String[]{
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.CAMERA};

        if (Build.VERSION.SDK_INT >= 23) {
            if (ContextCompat.checkSelfPermission(currentActivity, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED &&
                    ContextCompat.checkSelfPermission(currentActivity, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                Log.d(TAG, "permissionCheckFragment: " + code);
                if (code == 1)
                    cameraCall();
                else if (code == 2)
                    galleyCall();

            } else {

                currentFragment.requestPermissions(neededPermissions, code);
                return;

            }
        }
    }

    private void showMessageOKCancel(String message, DialogInterface.OnClickListener okListener) {
        new AlertDialog.Builder(currentActivity)
                .setMessage(message)
                .setPositiveButton("OK", okListener)
                .setNegativeButton("Cancel", null)
                .create()
                .show();
    }

    public void cameraCall() {
        ContentValues values = new ContentValues();
        imageUri = currentActivity.getContentResolver().insert(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
        Intent intent1 = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent1.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);

        if (isFragment)
            currentFragment.startActivityForResult(intent1, 0);
        else
            currentActivity.startActivityForResult(intent1, 0);
    }

    public void galleyCall() {
        Log.d(TAG, "galleyCall: ");

        Intent intent2 = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent2.setType("image/*");

        if (isFragment)
            currentFragment.startActivityForResult(intent2, 1);
        else
            currentActivity.startActivityForResult(intent2, 1);

    }


    public void requestPermissionResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case 1:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    cameraCall();
                } else {
                    permissionCheckFragment(1);
                }
                break;

            case 2:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    galleyCall();
                } else {

                    permissionCheckFragment(2);
                }
                break;
        }
    }


    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        String fileName;
        Bitmap bitmap;
        switch (requestCode) {
            case 0:
                if (resultCode == currentActivity.RESULT_OK) {

                    Log.i("Camera Selected", "Photo");
                    try {
                        selectedPath = null;
                        selectedPath = getPath(imageUri);
                        // Log.i("selected","path"+selectedPath);
                        fileName = selectedPath.substring(selectedPath.lastIndexOf("/") + 1);
                        // Log.i("file","name"+fileName);
                        bitmap = compressImage(imageUri.toString(), 960, 540);
                        imageAttachmentCallBack.imageAttachment(from, fileName, bitmap, imageUri);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                break;
            case 1:
                if (resultCode == currentActivity.RESULT_OK) {
                    Log.i("Gallery", "Photo");
                    Uri selectedImage = data.getData();

                    try {
                        selectedPath = null;
                        selectedPath = getPath(selectedImage);
                        fileName = selectedPath.substring(selectedPath.lastIndexOf("/") + 1);
                        bitmap = compressImage(selectedImage.toString(), 960, 540);
                        imageAttachmentCallBack.imageAttachment(from, fileName, bitmap, selectedImage);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                break;
        }
    }

    public Bitmap getImageFromUri(Uri uri, float height, float width) {
        Bitmap bitmap = null;

        try {
            bitmap = compressImage(uri.toString(), height, width);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return bitmap;
    }

    public String getFileNameFromUri(Uri uri) {
        String path = null, file_name = null;

        try {

            path = getRealPathFromURI(uri.getPath());
            file_name = path.substring(path.lastIndexOf("/") + 1);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

        return file_name;

    }

    public boolean checkImage(String fileName, String filePath) {
        boolean flag;
        path = new File(filePath);

        File file = new File(path, fileName);
        if (file.exists()) {
            Log.i("file", "exists");
            flag = true;
        } else {
            Log.i("file", "not exist");
            flag = false;
        }

        return flag;
    }

    public Bitmap getImage(String fileName, String filePath) {

        path = new File(filePath);
        File file = new File(path, fileName);

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inPreferredConfig = Bitmap.Config.ARGB_8888;
        options.inSampleSize = 2;
        options.inTempStorage = new byte[16 * 1024];

        Bitmap bitmap = BitmapFactory.decodeFile(file.getAbsolutePath(), options);

        if (bitmap != null)
            return bitmap;
        else
            return null;
    }

    public void createImage(Bitmap bitmap, String fileName, String filepath, boolean fileReplace) {

        path = new File(filepath);

        if (!path.exists()) {
            path.mkdirs();
        }

        File file = new File(path, fileName);

        if (file.exists()) {
            if (fileReplace) {
                file.delete();
                file = new File(path, fileName);
                store_image(file, bitmap);
                Log.i("file", "replaced");
            }
        } else {
            store_image(file, bitmap);
        }

    }

    public void store_image(File file, Bitmap bmp) {
        try {
            FileOutputStream out = new FileOutputStream(file);
            bmp.compress(Bitmap.CompressFormat.PNG, 80, out);
            out.flush();
            out.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static String getBase64FromBitMap(Bitmap picture) {


        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        picture.compress(Bitmap.CompressFormat.PNG, 100, baos);
        byte[] bytes = baos.toByteArray();


        return "data:image/png;base64," + Base64.encodeToString(bytes, Base64.DEFAULT);
    }

    public interface ImageAttachmentListener {
        public void imageAttachment(int from, String filename, Bitmap file, Uri uri);
    }


}
