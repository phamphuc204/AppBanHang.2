package com.hoan.appbanhang.activity;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.navigation.NavigationView;
import com.google.gson.Gson;
import com.hoan.appbanhang.R;
import com.hoan.appbanhang.adapter.LoaiSpAdapter;
import com.hoan.appbanhang.adapter.SanPhamMoiAdapter;
import com.hoan.appbanhang.model.LoaiSp;
import com.hoan.appbanhang.model.SanPhamMoi;
import com.hoan.appbanhang.model.User;
import com.hoan.appbanhang.retrofit.ApiBanHang;
import com.hoan.appbanhang.retrofit.RetrofitClient;
import com.hoan.appbanhang.untils.Untils;
import com.nex3z.notificationbadge.NotificationBadge;

import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;
import android.widget.ViewFlipper;

import java.util.ArrayList;
import java.util.List;

import io.paperdb.Paper;
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class MainActivity extends AppCompatActivity {
    Toolbar toolbar;
    ViewFlipper viewFlipper;
    RecyclerView recyclerViewManHinhChinh;
    NavigationView navigationView;
    ListView listViewManHinhChinh;
    DrawerLayout drawerLayout;
    LoaiSpAdapter loaiSpAdapter;
    List<LoaiSp> mangloaisp;
    CompositeDisposable compositeDisposable = new CompositeDisposable();
    ApiBanHang apiBanHang;
    List <SanPhamMoi> mangSpMoi;
    SanPhamMoiAdapter spAdapter;
    NotificationBadge badge;
    FrameLayout frameLayout;
    ImageView imgsearch;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        apiBanHang = RetrofitClient.getInstance(Untils.BASE_URL).create(ApiBanHang.class);
        Paper.init(this);
        if (Paper.book().read( "user") != null) { User user = Paper.book().read( "user");
            Untils.user_current = user;
        }
        Anhxa();
        ActionBar();

        if (isConnected ( this)) {

            ActionViewFlipper();
            getLoaiSanPham();
            getSpMoi();
            getEventClick();  
        }else {
            Toast.makeText(getApplicationContext(),  "Không có internet, vui lòng kết nối", Toast.LENGTH_LONG).show();
        }
    }

    private void getEventClick() {
        listViewManHinhChinh.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                switch (i) {
                    case 0:
                        Intent trangchu = new Intent(getApplicationContext(), MainActivity.class);
                        startActivity(trangchu);
                        break;
                    case 1:
                        Intent sonmoi = new Intent(getApplicationContext(), SonMoiActivity.class);
                        sonmoi.putExtra("loai",1);
                        startActivity(sonmoi);
                        break;
                    case 2 :
                        Intent phanphu = new Intent(getApplicationContext(), SonMoiActivity.class);
                        phanphu.putExtra("loai",2);
                        startActivity(phanphu);
                        break;
                    case 5:
                        Intent donhang = new Intent(getApplicationContext(), XemDonActivity.class);
                        startActivity(donhang);
                        break;
                    case 6:
                        // xóa key user
                        Paper.book().delete("user");
                        Intent dangnhap = new Intent(getApplicationContext(), DangNhapActivity.class);
                        startActivity(dangnhap);
                        finish();
                        break;

                }
            }
        });

    }

    private void getSpMoi() {
        Log.d("MainActivity", "getSpMoi: Bắt đầu gọi API");
        compositeDisposable.add(apiBanHang.getSpMoi()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        sanPhamMoiModel -> {
                            Log.d("MainActivity", "getSpMoi: API trả về phản hồi");

                            if (sanPhamMoiModel.isSucess()) {
                                List<SanPhamMoi> mangSpMoi = sanPhamMoiModel.getResult();
                                spAdapter = new SanPhamMoiAdapter(getApplicationContext(), mangSpMoi);
                                recyclerViewManHinhChinh.setAdapter(spAdapter);
                                Log.d("MainActivity", "getSpMoi: Adapter đã được thiết lập");
                            } else {
                                Log.e("MainActivity", "getSpMoi: Không thành công, message: " + sanPhamMoiModel.getMessage());
                            }
                        },
                        throwable -> {
                            Log.e("MainActivity", "getSpMoi: Lỗi khi gọi API", throwable);
                            Toast.makeText(getApplicationContext(), "Không kết nối được với server: " + throwable.getMessage(), Toast.LENGTH_LONG).show();
                        }
                )
        );
    }



    private void getLoaiSanPham() {
        compositeDisposable.add(apiBanHang.getLoaiSp()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        loaiSpModel -> {
                            if (loaiSpModel.isSuccess()) {
                                mangloaisp = loaiSpModel.getResult();
                              mangloaisp.add(new LoaiSp("Đăng xuất", "https://png.pngtree.com/png-clipart/20230314/original/pngtree-log-out-vector-icon-design-illustration-png-image_8987853.png"));
                                loaiSpAdapter = new LoaiSpAdapter (getApplicationContext(), mangloaisp);
                                listViewManHinhChinh.setAdapter(loaiSpAdapter);
                            }
                        }
                )
        );
    }

    private void ActionViewFlipper() {
        List<String> mangquangcao = new ArrayList<>();
        mangquangcao.add("https://i.ytimg.com/vi/csgpY_I-E7s/hqdefault.jpg");
        mangquangcao.add("https://intphcm.com/data/upload/banner-my-pham-dep.jpg");
        mangquangcao.add("https://mir-s3-cdn-cf.behance.net/projects/404/fa11c6140311873.623f54cfb16a6.jpg");
        for (int i = 0; i < mangquangcao.size(); i++) {
            ImageView imageView = new ImageView(getApplicationContext());
            Glide.with(getApplicationContext()).load(mangquangcao.get(i)).into(imageView);
            imageView.setScaleType(ImageView.ScaleType.FIT_XY);
            viewFlipper.addView(imageView);
        }
        viewFlipper.setFlipInterval(3000);
        viewFlipper.setAutoStart(true);
        Animation slide_in = AnimationUtils.loadAnimation (getApplicationContext(), R.anim.slide_in_right);
        Animation slide_out = AnimationUtils.loadAnimation (getApplicationContext(), R.anim.slide_out_right);
        viewFlipper.setInAnimation(slide_in);
        viewFlipper.setOutAnimation(slide_out);
    }
    private void ActionBar() {
        setSupportActionBar (toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationIcon (android.R.drawable.ic_menu_sort_by_size);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                drawerLayout.openDrawer (GravityCompat.START);
            }
        });
    }
    private void Anhxa() {
        imgsearch = findViewById(R.id.imgsearch);
        toolbar = findViewById(R.id.toobarmanhinhchinh);
        viewFlipper = findViewById(R.id.viewlipper);
        recyclerViewManHinhChinh = findViewById(R.id.recycleview);
        RecyclerView.LayoutManager layoutManager = new GridLayoutManager(this,  2);
        recyclerViewManHinhChinh.setLayoutManager (layoutManager);
        recyclerViewManHinhChinh.setHasFixedSize(true);
        listViewManHinhChinh = findViewById(R.id.listviewmanhinhchinh);
        navigationView = findViewById(R.id.navigationview);
        drawerLayout = findViewById(R.id.drawerlayout);
        badge  = findViewById(R.id.menu_sl);
        frameLayout = findViewById(R.id.framegiohang);
        //khoi tao list
        mangloaisp = new ArrayList<>();
        mangSpMoi =  new ArrayList<>();
        if (Paper.book().read("giohang") != null){
            Untils.manggiohang = Paper.book().read("giohang");
        }
        if (Untils.manggiohang == null) {
            Untils.manggiohang = new ArrayList<>();
        }else {
            int totalItem = 0;
            for (int i=0; i<Untils.manggiohang.size(); i++){
                totalItem = totalItem+ Untils.manggiohang.get(i).getSoluong();
            }
            badge.setText(String.valueOf(totalItem));
        }

        frameLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent giohang = new Intent(getApplicationContext(), GioHangActivity.class);
                startActivity(giohang);
            }
        });
        imgsearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), SearchActivity.class);
                startActivity(intent);
            }
        });

    }

    @Override
    protected void onResume() {
        super.onResume();
        int totalItem = 0;
        for (int i=0; i<Untils.manggiohang.size(); i++){
            totalItem = totalItem+ Untils.manggiohang.get(i).getSoluong();
        }
        badge.setText(String.valueOf(totalItem));
    }

    private boolean isConnected(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);


            // Deprecated in API 29. For API 29+, use NetworkCapabilities.
            NetworkInfo wifi = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
            NetworkInfo mobile = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);

            if ((wifi != null && wifi.isConnected()) || (mobile != null && mobile.isConnected())) {
                return true;
            }else {
                return false;
            }


    }
    @Override
    protected void onDestroy() {
        compositeDisposable.clear();
        super.onDestroy();
    }


}
