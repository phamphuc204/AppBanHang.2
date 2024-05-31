package com.hoan.appbanhang.activity;

import android.app.Application;
import android.os.Build;

import com.paypal.checkout.PayPalCheckout;
import com.paypal.checkout.config.CheckoutConfig;
import com.paypal.checkout.config.Environment;
import com.paypal.checkout.createorder.CurrencyCode;
import com.paypal.checkout.createorder.UserAction;

public class paypal extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PayPalCheckout.setConfig(new CheckoutConfig(
                    this,
                    "AfiTJUaV2uL2rJ3XWhqDWkbUYGU1st67-0-w_KnnpGQBv3M8BQN_mmDJEnSunGJfO9-ufNf8HgrAgszH",
                    Environment.SANDBOX,
                    CurrencyCode.USD,
                    UserAction.PAY_NOW,
                    "com.thanhanhthaibao.paypaltest://paypalpay"
            ));
        }
    }



}
