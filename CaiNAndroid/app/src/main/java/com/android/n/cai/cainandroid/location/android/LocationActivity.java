package com.android.n.cai.cainandroid.location.android;

import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.widget.TextView;

import com.android.n.cai.cainandroid.common.BaseActivity;
import com.android.n.cai.cainandroid.utils.LogUtils;
import com.android.n.cai.cainandroid.R;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.android.n.cai.cainandroid.R.id.textView;

/**
 * Created by Kuriboh on 2017/9/18.
 * E-Mail Address: cai_android@163.com
 */

public class LocationActivity extends BaseActivity {
    @BindView(textView)
    TextView mTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_location);
        ButterKnife.bind(this);
        Location location = LocationUtils.getInstance(LocationActivity.this).showLocation();
        if (location != null) {
            String address = "纬度：" + location.getLatitude() + "经度：" + location.getLongitude();
            LogUtils.d("FLY.LocationUtils", address);
            mTextView.setText(address);
        }

    }

    public static void startActivity(Context context, Bundle bundle) {
        Intent intent = new Intent(context, LocationActivity.class);
        intent.putExtras(bundle);
        context.startActivity(intent);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        LocationUtils.getInstance(this).removeLocationUpdatesListener();
    }
}
