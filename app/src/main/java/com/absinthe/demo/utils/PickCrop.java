package com.absinthe.demo.utils;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.content.FileProvider;
import android.util.Log;

import com.absinthe.demo.MainActivity;
import com.absinthe.demo.R;
import com.zhihu.matisse.Matisse;
import com.zhihu.matisse.MimeType;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;

import static com.absinthe.demo.utils.Const.CAPTURE_PHOTO_REQUEST_CODE;
import static com.absinthe.demo.utils.Const.PERMISSION_CAMERA_CODE;
import static com.absinthe.demo.utils.Const.PERMISSION_CAMERA_MSG;
import static com.absinthe.demo.utils.Const.PERMISSION_STORAGE_CODE;
import static com.absinthe.demo.utils.Const.PERMISSION_STORAGE_MSG;
import static com.absinthe.demo.utils.Const.REQUEST_CODE_CHOOSE;

public class PickCrop {
    @AfterPermissionGranted(PERMISSION_STORAGE_CODE)
    public static void openAlbum(Activity activity) {
        String[] perms = {Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE};
        if (EasyPermissions.hasPermissions(activity, perms)) {
            // Already have permission, do the thing
            Matisse.from(activity)
                    .choose(MimeType.ofImage())
                    .countable(true)
                    .maxSelectable(1)
                    .gridExpectedSize(activity.getResources().getDimensionPixelSize(R.dimen.grid_expected_size))
                    .restrictOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED)
                    .thumbnailScale(0.85f)
                    .imageEngine(new Glide4Engine())
                    .forResult(REQUEST_CODE_CHOOSE);
        } else {
            // Do not have permissions, request them now
            EasyPermissions.requestPermissions(activity,PERMISSION_STORAGE_MSG , PERMISSION_STORAGE_CODE, perms);
        }
    }

    @AfterPermissionGranted(PERMISSION_CAMERA_CODE)
    public static void openCamera(Activity activity) {
        String[] perms = {Manifest.permission.CAMERA};
        if (EasyPermissions.hasPermissions(activity, perms)) {
            // Already have permission, do the thing
            File outputFile = new File(activity.getExternalCacheDir(), "raw.png");

            try {
                if (outputFile.exists()){
                    outputFile.delete();//删除
                }
                outputFile.createNewFile();
            } catch (Exception e) {
                e.printStackTrace();
            }
            MainActivity.imageUri = FileProvider.getUriForFile(activity,
                    "com.absinthe.demo.fileprovider", //可以是任意字符串
                    outputFile);
            Intent captureIntent = new Intent("android.media.action.IMAGE_CAPTURE");
            captureIntent.putExtra(MediaStore.EXTRA_OUTPUT, MainActivity.imageUri);
            activity.startActivityForResult(captureIntent, CAPTURE_PHOTO_REQUEST_CODE);
        } else {
            // Do not have permissions, request them now
            EasyPermissions.requestPermissions(activity,PERMISSION_CAMERA_MSG , PERMISSION_CAMERA_CODE, perms);
        }
    }

    public static Intent CropFromAlbum(Uri uri) {
        try {
            //直接裁剪
            Intent intent = new Intent("com.android.camera.action.CROP");
            //设置裁剪之后的图片路径文件
            File cropFile = new File(Environment.getExternalStorageDirectory().getPath(),
                    "file.png"); //随便命名一个
            if (cropFile.exists()){ //如果已经存在，则先删除,这里应该是上传到服务器，然后再删除本地的，没服务器，只能这样了
                cropFile.delete();
            }
            cropFile.createNewFile();
            //初始化 uri
            Uri tempUri = uri; //返回来的 uri
            Uri outputUri = null; //真实的 uri
            Log.d("MainActivity", "CropFromAlbum: " + cropFile);
            outputUri = Uri.fromFile(cropFile);
            MainActivity.imageUri = outputUri;
            Log.d("MainActivity", "mCameraUri: " + MainActivity.imageUri);
            // crop为true是设置在开启的intent中设置显示的view可以剪裁
            intent.putExtra("crop",true);
            // aspectX,aspectY 是宽高的比例，这里设置正方形
            intent.putExtra("aspectX",0);
            intent.putExtra("aspectY",0);
            //设置要裁剪的宽高
            //intent.putExtra("outputX", 200);
            //intent.putExtra("outputY",200);
            intent.putExtra("scale",true);
            //如果图片过大，会导致oom，这里设置为false
            intent.putExtra("return-data",false);
            if (tempUri != null) {
                intent.setDataAndType(tempUri, "image/*");
            }
            if (outputUri != null) {
                intent.putExtra(MediaStore.EXTRA_OUTPUT, outputUri);
            }
            intent.putExtra("noFaceDetection", true);
            //压缩图片
            intent.putExtra("outputFormat", Bitmap.CompressFormat.JPEG.toString());
            return intent;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    //代码重复性太高，有时间改
    public static Intent CropFromCamera(Activity activity, String cameraPath,String imgName) {
        try {

            //设置裁剪之后的图片路径文件
            File cropFile = new File(Environment.getExternalStorageDirectory().getPath(),
                    "file.png"); //随便命名一个
            if (cropFile.exists()){ //如果已经存在，则先删除,这里应该是上传到服务器，然后再删除本地的，没服务器，只能这样了
                cropFile.delete();
            }
            cropFile.createNewFile();
            //初始化 uri
            Uri tempUri = null; //返回来的 uri
            Uri outputUri = null; //真实的 uri
            Intent intent = new Intent("com.android.camera.action.CROP");
            //拍照留下的图片
            File cameraFile = new File(cameraPath,imgName);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            tempUri = FileProvider.getUriForFile(activity,
                    "com.absinthe.demo.fileprovider",
                    cameraFile);
            outputUri = Uri.fromFile(cropFile);
            //把这个 uri 提供出去，就可以解析成 bitmap了
            MainActivity.imageUri = outputUri;
            // crop为true是设置在开启的intent中设置显示的view可以剪裁
            intent.putExtra("crop",true);
            // aspectX,aspectY 是宽高的比例，这里设置正方形
            intent.putExtra("aspectX",0);
            intent.putExtra("aspectY",0);
            //设置要裁剪的宽高
            //intent.putExtra("outputX", 200);
            //intent.putExtra("outputY",200);
            intent.putExtra("scale",true);
            //如果图片过大，会导致oom，这里设置为false
            intent.putExtra("return-data",false);
            if (tempUri != null) {
                intent.setDataAndType(tempUri, "image/*");
            }
            if (outputUri != null) {
                intent.putExtra(MediaStore.EXTRA_OUTPUT, outputUri);
            }
            intent.putExtra("noFaceDetection", true);
            //压缩图片
            intent.putExtra("outputFormat", Bitmap.CompressFormat.JPEG.toString());
            return intent;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static Bitmap decodeUriAsBitmap(Activity activity, Uri uri){
        Bitmap bitmap = null;
        try {
            bitmap = BitmapFactory.decodeStream(activity.getContentResolver().openInputStream(uri));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return null;
        }
        return bitmap;
    }
}
