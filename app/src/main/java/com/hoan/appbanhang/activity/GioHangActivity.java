package com.hoan.appbanhang.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.appcompat.widget.Toolbar;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.hoan.appbanhang.R;
import com.hoan.appbanhang.adapter.GioHangAdapter;
import com.hoan.appbanhang.model.EventBus.TinhTongEvent;
import com.hoan.appbanhang.model.GioHang;
import com.hoan.appbanhang.untils.Untils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.text.DecimalFormat;


public class GioHangActivity extends AppCompatActivity {
    TextView giohangtrong, tongtien;
    Toolbar toolbar;
    RecyclerView recyclerView;
    Button btnmuahang;
    GioHangAdapter adapter;
    long tongtiensp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState );
        setContentView(R.layout.activity_gio_hang);
        initView();
        initControl();
        if (Untils.mangmuahang != null) {
            Untils.mangmuahang.clear();
        }
        tinhTongTien();
    }
    private void tinhTongTien() {
        tongtiensp = 0;
        for (int i = 0; i<Untils.mangmuahang.size(); i++){
            tongtiensp = tongtiensp+ (Untils.mangmuahang.get(i).getGiasp()* Untils.mangmuahang.get(i).getSoluong());
        }
        DecimalFormat decimalFormat = new DecimalFormat( "###,###,###");
        tongtien.setText(decimalFormat.format(tongtiensp));
    }
    private void initControl() {
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        recyclerView.setHasFixedSize(true);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager( this);
        recyclerView.setLayoutManager (layoutManager);
        if (Untils.manggiohang.size() == 0){
            giohangtrong.setVisibility(View.VISIBLE);
        }else {
            adapter = new GioHangAdapter(getApplicationContext(),Untils.manggiohang);
            recyclerView.setAdapter(adapter);

        }
        btnmuahang.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent =new Intent(getApplicationContext(), ThanhToanActivity.class);
                intent.putExtra("tongtien", tongtiensp);
               // Untils.manggiohang.clear();
                startActivity(intent);
            }
        });
    }

    private void initView() {
        giohangtrong = findViewById(R.id.txtgiohangtrong);
        tongtien = findViewById(R.id.txttongtien);
        toolbar = findViewById(R.id.toobar);
        recyclerView = findViewById(R.id.recycleviewgiohang);
        btnmuahang = findViewById(R.id.btnmuahang);
    }

    @Override
    protected void onStart() {
        super.onStart();
        super.onStart();
        EventBus.getDefault().register( this);
    }
    @Override
    protected void onStop() {
        EventBus.getDefault().unregister ( this);
        super.onStop();
    }
    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void eventTinhTien(TinhTongEvent event) {
        if (event != null) {
            tinhTongTien();
        }
    }


}