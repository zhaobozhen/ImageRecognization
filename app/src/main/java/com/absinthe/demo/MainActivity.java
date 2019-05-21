package com.absinthe.demo;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.absinthe.demo.utils.PickCrop;
import com.absinthe.demo.utils.UIUtils;
import com.zhihu.matisse.Matisse;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Objects;

import pub.devrel.easypermissions.EasyPermissions;

import static com.absinthe.demo.utils.Const.*;
import static com.absinthe.demo.utils.PickCrop.*;

public class MainActivity extends AppCompatActivity {
    public static Uri imageUri;

    private final String server = "http://45.32.49.235:7777/test";

    private Button selectImg;
    private ImageView image;
    private Button upload;
    @SuppressLint("HandlerLeak")
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_UPLOAD_START:
                    UIUtils.showProgressDialog(MainActivity.this, "上传中……");
                    break;
                case MSG_UPLOAD_FAIL:
                    UIUtils.closeProgressDialog();
                    Toast.makeText(MainActivity.this, "上传失败", Toast.LENGTH_LONG).show();
                    break;
                case MSG_UPLOAD_SUCCESS:
                    UIUtils.closeProgressDialog();
                    break;
            }
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        selectImg = findViewById(R.id.select_img);
        image = findViewById(R.id.image_view);
        upload = findViewById(R.id.upload);

        selectImg.setOnClickListener(v -> showListDialog());
        upload.setOnClickListener(v -> new Thread(() -> uploadImage(server, "file.png",
                        new File(Environment
                        .getExternalStorageDirectory()
                        .getAbsolutePath()
                        + "/file.png"))).start());
    }

    private void uploadImage(String actionUrl, String newName, File uploadFile) {
        String end = "\r\n";
        String twoHyphens = "--";
        String boundary = "*****";

        new Thread(() -> {
            try {
                handler.sendEmptyMessage(MSG_UPLOAD_START);
                URL url = new URL(actionUrl);
                HttpURLConnection con = (HttpURLConnection) url.openConnection();
                /* 允许Input、Output，不使用Cache */
                con.setDoInput(true);
                con.setDoOutput(true);
                con.setUseCaches(false);
                /* 设置传送的method=POST */
                con.setRequestMethod("POST");
                /* setRequestProperty */
                con.setRequestProperty("Connection", "Keep-Alive");
                con.setRequestProperty("Charset", "UTF-8");
                con.setRequestProperty("Content-Type",
                        "multipart/form-data;boundary=" + boundary);

                //Log.d("Server", "ResponseCode:"+con.getResponseCode());

                /* 设置DataOutputStream */
                OutputStream outputSteam = con.getOutputStream();
                DataOutputStream ds = new DataOutputStream(outputSteam);
                Log.d("Server", "here");
                StringBuilder sb = new StringBuilder();
                sb.append(twoHyphens).append(boundary)
                        .append(end);
                sb.append("Content-Disposition: form-data; " + "name=\"file\";filename=\"")
                        .append(newName)
                        .append("\"")
                        .append(end);
                sb.append("Content-Type: application/octet-stream; charset=\"UTF-8\"")
                        .append(end);
                sb.append(end);
                ds.write(sb.toString().getBytes());

                Log.d("Server", "DS:" + sb);
                System.out.println(ds);

                /* 取得文件的FileInputStream */
                FileInputStream fStream = new FileInputStream(uploadFile);
                /* 设置每次写入1024bytes */
                int bufferSize = 1024;
                byte[] buffer = new byte[bufferSize];

                int length;
                /* 从文件读取数据至缓冲区 */
                while ((length = fStream.read(buffer)) != -1) {
                    /* 将资料写入DataOutputStream中 */
                    ds.write(buffer, 0, length);
                }
                ds.writeBytes(end);

                // -----
                ds.writeBytes(twoHyphens + boundary + end);
                ds.writeBytes("Content-Disposition: form-data;name=\"file\"" + end);
                ds.writeBytes(end + URLEncoder.encode("xiexiezhichi", "UTF-8")
                        + end);
                // -----

                ds.writeBytes(twoHyphens + boundary + twoHyphens + end);
                /* close streams */
                fStream.close();
                ds.flush();

                /* 取得Response内容 */
                InputStream is = con.getInputStream();
                int ch;
                StringBuilder b = new StringBuilder();
                while ((ch = is.read()) != -1) {
                    b.append((char) ch);
                }

                int res = con.getResponseCode();
                Log.d("Server", "UploadSuccess:" + res);

                if (res == 200) {
                    handler.sendEmptyMessage(MSG_UPLOAD_SUCCESS);
                    Intent intent = new Intent(MainActivity.this, ResultActivity.class);
                    startActivity(intent);
                }

                /* 关闭DataOutputStream */
                ds.close();
            } catch (Exception e) {
                e.printStackTrace();
                handler.sendEmptyMessage(MSG_UPLOAD_FAIL);
            }
        }).start();
    }



    private void showListDialog() {
        final String[] listItems = new String[]{"选择相册图片", "拍照"};

        AlertDialog.Builder listDialog = new AlertDialog.Builder(this);
        listDialog.setTitle(getString(R.string.btn_upload_img));

    /*
        设置item 不能用setMessage()
        用setItems
        items : listItems[] -> 列表项数组
        listener -> 回调接口
    */
        listDialog.setItems(listItems, (dialog, which) -> {
            switch (which) {
                case 0:                    //选择相册图片实现
                    PickCrop.openAlbum(MainActivity.this);
                    break;
                case 1:                    //拍照上传实现
                    PickCrop.openCamera(MainActivity.this);
                    break;
            }
        });

        listDialog.create().show();
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case REQUEST_CODE_CHOOSE:
                    imageUri = Matisse.obtainResult(Objects.requireNonNull(data)).get(0);
                    startActivityForResult(CropFromAlbum(imageUri), REQUEST_PICKER_AND_CROP);
                    Log.d("Matisse", "mSelected: " + imageUri);
                    break;
                case CAPTURE_PHOTO_REQUEST_CODE:
                    startActivityForResult(CropFromCamera(MainActivity.this, getExternalCacheDir().getPath(), "raw.png"), REQUEST_PICKER_AND_CROP);
                case REQUEST_PICKER_AND_CROP:
                    Bitmap bitmap = decodeUriAsBitmap(MainActivity.this, imageUri);
                    image.setImageBitmap(bitmap);
                    image.setVisibility(View.VISIBLE);
                    upload.setVisibility(View.VISIBLE);
                    break;
            }
        }
    }

}