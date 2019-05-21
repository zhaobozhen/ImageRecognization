package com.absinthe.demo.utils;

import android.app.ProgressDialog;


public class UIUtils {
    public static void showProgressDialog(ProgressDialog progressDialog, String text) {
        if (progressDialog != null) {
            progressDialog.setMessage(text);
            progressDialog.setCanceledOnTouchOutside(false);
            progressDialog.setCancelable(true);
            progressDialog.show();
        }
    }

    public static void closeProgressDialog(ProgressDialog progressDialog) {
        if (progressDialog != null) {
            progressDialog.dismiss();
        }
    }
}
