package com.hoan.appbanhang.activity;

import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.hoan.appbanhang.R;
import com.hoan.appbanhang.adapter.SonMoiAdapter;
import com.hoan.appbanhang.model.SanPhamMoi;
import com.hoan.appbanhang.retrofit.ApiBanHang;
import com.hoan.appbanhang.retrofit.RetrofitClient;
import com.hoan.appbanhang.untils.Untils;
import io.reactivex.rxjava3.schedulers.Schedulers;

import java.util.ArrayList;
import java.util.List;
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.disposables.CompositeDisposable;

public class SonMoiActivity extends AppCompatActivity {
    Toolbar toolbar;
    RecyclerView recyclerView;
    ApiBanHang apiBanHang;
    CompositeDisposable compositeDisposable =  new CompositeDisposable();
    int page = 1 ;
    int loai;
    SonMoiAdapter adapterSm;
    List<SanPhamMoi> sanPhamMoiList;
    LinearLayoutManager linearLayoutManager;
    Handler handler = new Handler();
    boolean isLoading = false;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_son_moi);
        apiBanHang = RetrofitClient.getInstance(Untils.BASE_URL).create(ApiBanHang.class);
        loai = getIntent().getIntExtra("loai", 1);
        AnhXa();
        ActionToolBar();
        getData(page);
        addEventLoad();

    }

    private void addEventLoad() {
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
            }

            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if (isLoading == false) {
                    if (linearLayoutManager.findLastCompletelyVisibleItemPosition() == sanPhamMoiList.size() - 1) {
                        isLoading = true;
                        loadMore();
                    }
                }
            }
        });
    }

    private void loadMore() {
        handler.post(new Runnable() {
            @Override
            public void run() {
                // add null
                sanPhamMoiList.add(null);
                adapterSm.notifyItemInserted(sanPhamMoiList.size() - 1);
            }
        });

        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                // remove null
                sanPhamMoiList.remove(sanPhamMoiList.size() - 1);
                adapterSm.notifyItemRemoved(sanPhamMoiList.size());
                page = page + 1;
                getData(page);
            }
        }, 2000);
    }

    private void getData(int page) {
        compositeDisposable.add(apiBanHang.getSanPham(page, loai)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        sanPhamMoiModel -> {
                            if (sanPhamMoiModel.isSucess()) {
                                if (adapterSm == null) {
                                    sanPhamMoiList = sanPhamMoiModel.getResult();
                                    adapterSm = new SonMoiAdapter(getApplicationContext(), sanPhamMoiList);
                                    recyclerView.setAdapter(adapterSm);
                                } else {
                                    int vitri = sanPhamMoiList.size();
                                    int soluongadd = sanPhamMoiModel.getResult().size();
                                    sanPhamMoiList.addAll(sanPhamMoiModel.getResult());
                                    adapterSm.notifyItemRangeInserted(vitri, soluongadd);
                                }
                                isLoading = false;  // Reset isLoading after data is loaded
                            } else {
                                Toast.makeText(getApplicationContext(), "Đã hết dữ liệu", Toast.LENGTH_LONG).show();
                                isLoading = true;
                            }
                        },
                        throwable -> {
                            Toast.makeText(getApplicationContext(), "Không kết nối server", Toast.LENGTH_LONG).show();
                            isLoading = false;  // Reset isLoading in case of error
                        }
                ));
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

        if (loai == 1) {
            getSupportActionBar().setTitle("Son môi");
        } else if (loai == 2) {
            getSupportActionBar().setTitle("Phấn phủ");
        }

    }

    private void AnhXa() {
        toolbar = findViewById(R.id.toolbar);  // Ensure this ID matches your XML
        recyclerView = findViewById(R.id.recycleview_sm);
        linearLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL,false);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setHasFixedSize(true);
        sanPhamMoiList = new ArrayList<>();


    }

    @Override
    protected void onDestroy() {
        compositeDisposable.clear();
        super.onDestroy();
    }
}
