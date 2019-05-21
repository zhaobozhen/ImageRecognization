package com.absinthe.demo.utils;

import android.app.Activity;
import android.app.ProgressDialog;


public class UIUtils {
    private static ProgressDialog progressDialog;

    public static void showProgressDialog(Activity activity, String text) {
        if (progressDialog == null) {
            progressDialog = new ProgressDialog(activity);
            progressDialog.setMessage(text);
            progressDialog.setCanceledOnTouchOutside(false);
            progressDialog.setCancelable(true);
        }
        progressDialog.show();
    }

    public static void closeProgressDialog() {
        if (progressDialog != null) {
            progressDialog.dismiss();
        }
    }
}
