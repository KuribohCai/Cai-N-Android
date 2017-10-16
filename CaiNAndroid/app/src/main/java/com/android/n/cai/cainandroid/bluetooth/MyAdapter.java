package com.android.n.cai.cainandroid.bluetooth;

import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.android.n.cai.cainandroid.R;

import java.util.ArrayList;

/**
 * Author: duke
 * Date: 2016-05-26 19:04
 * Description:
 */
public class MyAdapter extends BaseAdapter {
    private ArrayList<BluetoothDevice> mListData = new ArrayList<>();
    private Activity activity;

    public MyAdapter(Activity activity, ArrayList<BluetoothDevice> mListData) {
        if(activity != null){
            this.activity = activity;
        }
        if(mListData != null){
            this.mListData = mListData;
        }else{
            this.mListData.clear();
        }
    }

    public void refreshData(ArrayList<BluetoothDevice> mListData) {
        if(mListData != null){
            this.mListData = mListData;
        }else{
            this.mListData.clear();
        }
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return mListData.size();
    }

    @Override
    public Object getItem(int position) {
        return mListData.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder = null;
        if (convertView == null) {
            convertView = LayoutInflater.from(activity).inflate(R.layout.item_device,parent,false);
            //错误,参考LayoutInflater.from(activity).inflate()博客
            //convertView = View.inflate(activity, R.layout.item_device, parent);
            viewHolder = new ViewHolder();
            viewHolder.id = (TextView) convertView.findViewById(R.id.item_id);
            viewHolder.name = (TextView) convertView.findViewById(R.id.item_name);
            viewHolder.address = (TextView) convertView.findViewById(R.id.item_address);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        if (mListData != null) {
            BluetoothDevice bean = mListData.get(position);
            if (bean != null) {
                viewHolder.id.setText(String.valueOf(bean.hashCode()));
                viewHolder.name.setText(bean.getName());
                viewHolder.address.setText(bean.getAddress());
            }
        }
        return convertView;
    }

    public static class ViewHolder {
        public TextView id;
        public TextView name;
        public TextView address;
    }
}