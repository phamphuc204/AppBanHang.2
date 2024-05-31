package com.hoan.appbanhang.activity;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.StrictMode;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.hoan.appbanhang.R;
import com.hoan.appbanhang.model.CreateOrder;
import com.hoan.appbanhang.model.GioHang;
import com.hoan.appbanhang.retrofit.ApiBanHang;
import com.hoan.appbanhang.retrofit.RetrofitClient;
import com.hoan.appbanhang.untils.Untils;
import com.paypal.checkout.approve.Approval;
import com.paypal.checkout.approve.OnApprove;
import com.paypal.checkout.createorder.CreateOrderActions;
import com.paypal.checkout.createorder.CurrencyCode;
import com.paypal.checkout.createorder.OrderIntent;
import com.paypal.checkout.createorder.UserAction;
import com.paypal.checkout.order.Amount;
import com.paypal.checkout.order.AppContext;
import com.paypal.checkout.order.CaptureOrderResult;
import com.paypal.checkout.order.OnCaptureComplete;
import com.paypal.checkout.order.OrderRequest;
import com.paypal.checkout.order.PurchaseUnit;
import com.paypal.checkout.paymentbutton.PaymentButtonContainer;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import io.paperdb.Paper;
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.schedulers.Schedulers;
import kotlin.Unit;
import kotlin.jvm.functions.Function1;
import retrofit2.Call;
import retrofit2.Response;
import vn.zalopay.sdk.Environment;
import vn.zalopay.sdk.ZaloPayError;
import vn.zalopay.sdk.ZaloPaySDK;
import vn.zalopay.sdk.listeners.PayOrderListener;

public class ThanhToanActivity extends AppCompatActivity {
        Toolbar toolbar;
        TextView txttongtien, txtsdt, txtemail;
        EditText edtdiachi;
        AppCompatButton btndathang, btnzalo;
        CompositeDisposable compositeDisposable = new CompositeDisposable();
        ApiBanHang apiBanHang;
        long tongtien;
        int iddonhang;
    int totalItem;

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_thanh_toan);
            // zalo
            StrictMode.ThreadPolicy policy = new
            StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);

            // ZaloPay SDK Init
            ZaloPaySDK.init(2553, Environment.SANDBOX);
            initView();
            initControl();
            countItem();
        }

    private void countItem() {
        totalItem = 0;
        for (int i = 0; i < Untils.mangmuahang.size(); i++) {
            totalItem = totalItem + Untils.mangmuahang.get(i).getSoluong();
        }
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

            DecimalFormat decimalFormat = new DecimalFormat("###,###,###");
            tongtien = getIntent().getLongExtra("tongtien", 0);
            txttongtien.setText(decimalFormat.format(tongtien));
            String email = Untils.user_current.getEmail();
            String mobile = Untils.user_current.getMobile();

            Log.d("ThanhToanActivity", "Email: " + email);
            Log.d("ThanhToanActivity", "Mobile: " + mobile);
            txtemail.setText(Untils.user_current.getEmail());
            txtsdt.setText(Untils.user_current.getMobile());

            btndathang.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Log.d("ThanhToanActivity", "Button clicked");
                    String str_diachi = edtdiachi.getText().toString().trim();
                    if (TextUtils.isEmpty(str_diachi)) {
                        Log.d("ThanhToanActivity", "Address is empty, showing toast");
                        Toast.makeText(getApplicationContext(), "Bạn chưa nhập địa chỉ", Toast.LENGTH_SHORT).show();
                    } else {

                        // post data
                       String str_email = Untils.user_current.getEmail();
                       String str_sdt =Untils.user_current.getMobile();
                        int id = Untils.user_current.getId();
                        String jsonChitiet = convertGioHangToJson(Untils.mangmuahang);

                        compositeDisposable.add(apiBanHang.createOder(str_email, str_sdt, String.valueOf(tongtien ), id, str_diachi,  totalItem,jsonChitiet)
                                .subscribeOn(Schedulers.io())
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe(
                                        messageModel-> {
                                            Log.d("ThanhToanActivity", "Phản hồi từ API: " + new Gson().toJson(messageModel));

                                            if (messageModel.isSuccess()) {
                                                Toast.makeText(getApplicationContext(), "Đơn hàng đã được tạo thành công!", Toast.LENGTH_SHORT).show();
                                                for (int i = 0; i < Untils.mangmuahang.size(); i++) {
                                                    GioHang gioHang = Untils.mangmuahang.get(i);
                                                    if (Untils.manggiohang.contains(gioHang)) {
                                                        Untils.manggiohang.remove(gioHang);
                                                    }
                                                }
                                                Untils.mangmuahang.clear();
                                                Paper.book().write("giohang", Untils.manggiohang);
                                                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                                                startActivity(intent);
                                                finish();
                                            } else {
                                                // Xử lý khi không thành công
                                                Toast.makeText(getApplicationContext(), "Lỗi: " + messageModel.getMessage(), Toast.LENGTH_SHORT).show();
                                            }
                                        },
                                        throwable -> {
                                            // handle error
                                            Toast.makeText(getApplicationContext(), throwable.getMessage(), Toast.LENGTH_SHORT).show();
                                            Log.d("Tag", throwable.getMessage());
                                        }
                                )
                        );
                    }
                }
            });



        btnzalo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String str_diachi = edtdiachi.getText().toString().trim();
                if (TextUtils.isEmpty(str_diachi)) {
                    Toast.makeText(getApplicationContext(), "Bạn chưa nhập địa chỉ", Toast.LENGTH_SHORT).show();
                } else {
                    // Lưu các thông tin cần thiết vào biến tạm
                    String str_email = Untils.user_current.getEmail();
                    String str_sdt = Untils.user_current.getMobile();
                    int id = Untils.user_current.getId();
                    String jsonChitiet = convertGioHangToJson(Untils.mangmuahang);

                    // Khởi tạo thanh toán với ZaloPay
                    requestZalo(str_email, str_sdt, id, str_diachi, jsonChitiet);
                }
            }
        });

    }




    private void requestZalo(String str_email, String str_sdt, int id, String str_diachi, String jsonChitiet) {
        CreateOrder orderApi = new CreateOrder();

        try {
            JSONObject data = orderApi.createOrder(String.valueOf(tongtien));
            String code = data.getString("return_code");
            Log.d("test", code);
            if (code.equals("1")) {
                String token = data.getString("zp_trans_token");

                Log.d("test", token);
                ZaloPaySDK.getInstance().payOrder(ThanhToanActivity.this, token, "demozpdk://app", new PayOrderListener() {

                    @Override
                    public void onPaymentSucceeded(String s, String s1, String s2) {
                        // Chỉ tạo đơn hàng và cập nhật token sau khi thanh toán thành công
                        compositeDisposable.add(apiBanHang.createOder(str_email, str_sdt, String.valueOf(tongtien), id, str_diachi, totalItem, jsonChitiet)
                                .subscribeOn(Schedulers.io())
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe(
                                        messageModel -> {
                                            if (messageModel.isSuccess()) {
                                                iddonhang = Integer.parseInt(messageModel.getIddonhang());
                                                // Cập nhật mã token vào đơn hàng
                                                compositeDisposable.add(apiBanHang.updateMomo(iddonhang, token)
                                                        .subscribeOn(Schedulers.io())
                                                        .observeOn(AndroidSchedulers.mainThread())
                                                        .subscribe(
                                                                updateMessageModel -> {
                                                                    if (updateMessageModel.isSuccess()) {
                                                                        // Xóa giỏ hàng sau khi cập nhật token thành công
                                                                        Untils.mangmuahang.clear();
                                                                        Paper.book().write("giohang", Untils.manggiohang);
                                                                        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                                                                        startActivity(intent);
                                                                        finish();
                                                                    } else {
                                                                        // Xử lý khi cập nhật token không thành công
                                                                        Toast.makeText(getApplicationContext(), "Lỗi cập nhật đơn hàng: " + updateMessageModel.getMessage(), Toast.LENGTH_SHORT).show();
                                                                    }
                                                                },
                                                                throwable -> {
                                                                    Log.d("error", throwable.getMessage());
                                                                }
                                                        ));
                                            } else {
                                                Toast.makeText(getApplicationContext(), "Lỗi: " + messageModel.getMessage(), Toast.LENGTH_SHORT).show();
                                            }
                                        },
                                        throwable -> {
                                            Toast.makeText(getApplicationContext(), throwable.getMessage(), Toast.LENGTH_SHORT).show();
                                        }
                                ));
                    }

                    @Override
                    public void onPaymentCanceled(String s, String s1) {
                        // Xử lý khi thanh toán bị hủy
                        Toast.makeText(getApplicationContext(), "Thanh toán bị hủy", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onPaymentError(ZaloPayError zaloPayError, String s, String s1) {
                        // Xử lý khi có lỗi xảy ra trong quá trình thanh toán
                        Toast.makeText(getApplicationContext(), "Lỗi thanh toán: " + zaloPayError.toString(), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }






    private void initView() {
             apiBanHang = RetrofitClient.getInstance(Untils.BASE_URL).create(ApiBanHang.class);
            toolbar = findViewById(R.id.toobar);
            txttongtien = findViewById(R.id.txttongtien);
            txtsdt = findViewById(R.id.txtsodienthoai);
            txtemail = findViewById(R.id.txtemail);
            edtdiachi = findViewById(R.id.edtdiachi);
            btndathang = findViewById(R.id.btndathang);
            btnzalo = findViewById(R.id.btnzalopay);




        }
    private String convertGioHangToJson(List<GioHang> gioHangList) {
        JSONArray jsonArray = new JSONArray();
        for (GioHang gioHang : gioHangList) {
            JSONObject jsonObject = new JSONObject();
            try {
                jsonObject.put("giasp", gioHang.getGiasp());
                jsonObject.put("hinhsp", gioHang.getHinhsp());
                jsonObject.put("idsp", gioHang.getIdsp());
                jsonObject.put("soluong", gioHang.getSoluong());
                jsonObject.put("tensp", gioHang.getTensp());
            } catch (JSONException e) {
                e.printStackTrace();
            }
            jsonArray.put(jsonObject);
        }
        String jsonString = jsonArray.toString();
        Log.d("ThanhToanActivity", "Converted JSON: " + jsonString);
        return jsonString;
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        ZaloPaySDK.getInstance().onResult(intent);
    }

}

