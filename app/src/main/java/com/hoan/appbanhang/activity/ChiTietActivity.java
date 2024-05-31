package com.hoan.appbanhang.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.widget.Toolbar;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bumptech.glide.Glide;
import com.hoan.appbanhang.R;
import com.hoan.appbanhang.model.GioHang;
import com.hoan.appbanhang.model.SanPhamMoi;
import com.hoan.appbanhang.untils.Untils;
import com.nex3z.notificationbadge.NotificationBadge;

import java.text.DecimalFormat;

import io.paperdb.Paper;

public class ChiTietActivity extends AppCompatActivity {
    TextView tensp, giasp, mota;
    Button btnthem;
    ImageView imghinhanh;
    Spinner spinner;
    Toolbar toolbar;
    SanPhamMoi sanPhamMoi;
    NotificationBadge badge;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chi_tiet);
        initView();
        ActionToolBar();
        initData();
        initControl();

    }

    private void initControl() {
        btnthem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                themGioHang();
                 Paper.book().read("giohang",Untils.manggiohang);
            }
        });
    }

    private void themGioHang() {
        int soluong = Integer.parseInt(spinner.getSelectedItem().toString());
        boolean flag = false;

        for (int i = 0; i < Untils.manggiohang.size(); i++) {
            if (Untils.manggiohang.get(i).getIdsp() == sanPhamMoi.getId()) {
                int newQuantity = Untils.manggiohang.get(i).getSoluong() + soluong;
                if (newQuantity > 10) {
                        // Hiển thị Toast khi vượt quá 10 sản phẩm
                    Toast.makeText(getApplicationContext(), "Tối đa 10 sản phẩm mỗi đơn hàng", Toast.LENGTH_SHORT).show();
                    Untils.manggiohang.get(i).setSoluong(10); // Đặt lại số lượng tối đa là 10
                } else {
                    Untils.manggiohang.get(i).setSoluong(newQuantity);
                }
                flag = true;
                break;
            }
        }

        if (!flag) {
            if (soluong > 10) {
                // Hiển thị Toast khi vượt quá 10 sản phẩm

                soluong = 10; // Giới hạn số lượng là 10 nếu số lượng chọn vượt quá 10
            }
            GioHang gioHang = new GioHang();
            gioHang.setGiasp(Long.parseLong(sanPhamMoi.getGiasp()));
            gioHang.setSoluong(soluong);
            gioHang.setIdsp(sanPhamMoi.getId());
            gioHang.setTensp(sanPhamMoi.getTensp());
            gioHang.setHinhsp(sanPhamMoi.getHinhanh());
            Untils.manggiohang.add(gioHang);
        }

        int totalItem = 0;
        for (int i = 0; i < Untils.manggiohang.size(); i++) {
            totalItem = totalItem + Untils.manggiohang.get(i).getSoluong();
        }
        badge.setText(String.valueOf(totalItem));
    }


    private void initData() {
        sanPhamMoi = sanPhamMoi = (SanPhamMoi) getIntent().getSerializableExtra("chitiet");
        tensp.setText(sanPhamMoi.getTensp());
        mota.setText(sanPhamMoi.getMota());
        Glide.with(getApplicationContext()).load(sanPhamMoi.getHinhanh()).into(imghinhanh);
        DecimalFormat decimalFormat = new DecimalFormat( "###,###,###");
        giasp.setText("Giá: "+decimalFormat.format(Double.parseDouble (sanPhamMoi.getGiasp()))+ "VND" );
        Integer[] so = new Integer []{1,2,3,4,5,6,7,8,9,10};
        ArrayAdapter<Integer> adapterspin = new ArrayAdapter<>( this, androidx.appcompat.R.layout.support_simple_spinner_dropdown_item,so);
        spinner.setAdapter(adapterspin);
    }
    private void initView() {
        tensp = findViewById(R.id.txttensp);
        giasp = findViewById(R.id.txtgiasp);
        mota = findViewById(R.id.txtmotachitiet);
        btnthem = findViewById(R.id.btnthemvaogiohang);
        spinner =findViewById(R.id.spinner);
        imghinhanh = findViewById(R.id.imgchitiet);
        toolbar= findViewById(R.id.toobar);
        badge =findViewById(R.id.menu_sl);
        FrameLayout frameLayoutgiohang = findViewById(R.id.framegiohang);
        frameLayoutgiohang.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent giohang = new Intent(getApplicationContext(), GioHangActivity.class);
                startActivity(giohang);
            }
        });












        if (Untils.manggiohang != null) {
            int totalItem = 0;
            for (int i=0; i<Untils.manggiohang.size(); i++){
                totalItem = totalItem+ Untils.manggiohang.get(i).getSoluong();
            }
            badge.setText(String.valueOf(totalItem));
        }
    }
    private void ActionToolBar() {
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

    }
    protected void onResume() {
        super.onResume();
        if (Untils.manggiohang != null) {
            int totalItem = 0;
            for (int i = 0; i < Untils.manggiohang.size(); i++) {
                totalItem = totalItem + Untils.manggiohang.get(i).getSoluong();
            }
            badge.setText(String.valueOf(totalItem));
        }
    }
}
