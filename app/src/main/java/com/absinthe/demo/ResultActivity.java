package com.absinthe.demo;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Toast;

import com.absinthe.demo.utils.UIUtils;

import static com.absinthe.demo.utils.Const.*;

public class ResultActivity extends AppCompatActivity {
    private ProgressDialog progressDialog;
    @SuppressLint("HandlerLeak")
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_GET_START:
                    UIUtils.showProgressDialog(progressDialog, "等待服务器返回数据");
                    break;
                case MSG_GET_FAIL:
                    UIUtils.closeProgressDialog(progressDialog);
                    Toast.makeText(ResultActivity.this, "返回数据错误", Toast.LENGTH_LONG).show();
                    break;
                case MSG_GET_SUCCESS:
                    UIUtils.closeProgressDialog(progressDialog);
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);

        progressDialog = new ProgressDialog(this);

        getJSON();
    }

    private void getJSON() {
        new Thread(() -> {
            try {
                handler.sendEmptyMessage(MSG_GET_START);
                Thread.sleep(3000);
                handler.sendEmptyMessage(MSG_GET_SUCCESS);
            } catch (Exception e) {
                e.printStackTrace();
                handler.sendEmptyMessage(MSG_GET_FAIL);
            }
        }).start();
    }
}
