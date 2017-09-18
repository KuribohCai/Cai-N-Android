package com.android.n.cai.cainandroid.location.gaode;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;

import com.android.n.cai.cainandroid.common.BaseActivity;
import com.android.n.cai.cainandroid.R;

/**
 * 高德SDK
 * Created by Kuriboh on 2017/9/12.
 * E-Mail Address: cai_android@163.com
 */

public class GaoDeSDKActivity extends BaseActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gaodesdk);
    }

    public static  void startActivity(Context context, Bundle bundle) {
        Intent intent = new Intent(context,GaoDeSDKActivity.class);
        intent.putExtras(bundle);
        context.startActivity(intent);
    }
}
