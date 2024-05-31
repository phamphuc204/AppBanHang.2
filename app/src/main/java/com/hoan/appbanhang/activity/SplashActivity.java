package com.hoan.appbanhang.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import com.hoan.appbanhang.R;

import io.paperdb.Paper;

public class SplashActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        Paper.init(this);
        Thread thread = new Thread() {
            public void run() {
                try {
                    sleep(2500);
                } catch (Exception ex) {
                } finally {
                    if(Paper.book().read("user") == null) {
                        Intent intent = new Intent(getApplicationContext(), DangNhapActivity.class); startActivity(intent);
                        finish();
                    }else {
                        Intent home = new Intent(getApplicationContext(), MainActivity.class); startActivity(home);
                        finish();
                    }
                }
            }
        };
        thread.start();
    }
}
