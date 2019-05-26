package com.absinthe.demo;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import com.absinthe.demo.utils.Utility;

import org.json.JSONException;

public class ResultActivity extends AppCompatActivity {
    private ImageView standard_image;
    private TextView tv_name;
    private String name;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);

        standard_image = findViewById(R.id.iv_standard_image);
        tv_name = findViewById(R.id.tv_name);

        try {
            name = MainActivity.jsonObject.getString("filePath");
            Log.d("Result", "Name:"+name);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        tv_name.setText(name);

        int id = Utility.getResId(name, R.drawable.class);
        standard_image.setImageResource(id);
    }
}
