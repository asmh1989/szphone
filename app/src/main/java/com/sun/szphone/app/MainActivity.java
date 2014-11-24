package com.sun.szphone.app;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends Activity implements AdapterView.OnItemClickListener {

    private static final  String TAG = "SUN";

    private GridView mGridView;

    private MyAdapter mAdapter;

    private List<PhoneMessage> mPhones = new ArrayList<PhoneMessage>();
    private LayoutInflater mInflater;

    private AlertDialog mDialog;

    public LocationClient mLocationClient = null;
    public BDLocationListener myListener = new MyLocationListener();

    private boolean mLocationInit;

    private String mAdderss="";

    private String mNumber;

    public Handler mHandler=new Handler(){
        @Override
        public void handleMessage(Message msg){

            switch (msg.what){
                case 1:
                    if(mDialog != null){
                        mDialog.setMessage("拨出号码: "+mNumber+"\n"+mAdderss);
                    }
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);

//        mPhones.add(new PhoneMessage("10000", "10086", R.drawable.default_icon));
//        mPhones.add(new PhoneMessage("110", "匪警", R.drawable.alerm));
//        mPhones.add(new PhoneMessage("120", "医院", R.drawable.amb));
//        mPhones.add(new PhoneMessage("119", "火警", R.drawable.fire));

        mPhones.add(new PhoneMessage("10000", "10086", R.drawable.p));
        mPhones.add(new PhoneMessage("110", "匪警", R.drawable.p3));
        mPhones.add(new PhoneMessage("120", "医院", R.drawable.p4));
        mPhones.add(new PhoneMessage("119", "火警", R.drawable.p2));

        mGridView = (GridView) findViewById(R.id.gridview);
        mGridView.setAdapter(mAdapter = new MyAdapter());

        mGridView.setOnItemClickListener(this);

        if(getIntent() != null && getIntent().getStringExtra("number") != null){
            mNumber = getIntent().getStringExtra("number");

            String msg = "拨出号码: "+mNumber+"\n位置定位中";
            if(mAdderss.length() > 0){
                msg = "拨出号码: "+mNumber+"\n"+mAdderss;
            }

            mDialog = new AlertDialog.Builder(this).setTitle("监听").setMessage(msg)
            .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    mDialog.dismiss();
                }
            })
            .create();

            mDialog.show();

        }



        mLocationClient = new LocationClient(getApplicationContext());     //声明LocationClient类
        mLocationClient.registerLocationListener( myListener );    //注册监听函数


        try {
            LocationClientOption option = new LocationClientOption();
            option.setLocationMode(LocationClientOption.LocationMode.Hight_Accuracy);//设置定位模式
            option.setCoorType("bd09ll");//返回的定位结果是百度经纬度，默认值gcj02
            option.setScanSpan(5000);//设置发起定位请求的间隔时间为5000ms
            option.setIsNeedAddress(true);//返回的定位结果包含地址信息
            option.setNeedDeviceDirect(true);//返回的定位结果包含手机机头的方向

            mLocationClient.setLocOption(option);
            mLocationInit = true;
        } catch (Exception e){
            e.printStackTrace();
            mLocationInit = false;
            Log.e(TAG, "ERR:" + e.getMessage());
        }
        if (mLocationInit) {
            mLocationClient.start();
        } else {
            Toast.makeText(this, "定位参数设置失败", Toast.LENGTH_SHORT).show();
            return;
        }
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        Intent intent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + mPhones.get(i).number));
        startActivity(intent);
    }

    class PhoneMessage {
        public String number;
        public String info;
        public int icon = 0;
        public String icon_URL="";

        public PhoneMessage(String s1, String s2, int s3){
            number = s1;
            info = s2;
            icon = s3;
        }

        public PhoneMessage(String s1, String s2, String s3){
            number = s1;
            info = s2;
            icon_URL = s3;
        }
    }

    class MyAdapter extends BaseAdapter{

        @Override
        public int getCount() {
            return mPhones.size();
        }

        @Override
        public Object getItem(int i) {
            return mPhones.get(i);
        }

        @Override
        public long getItemId(int i) {
            return 0;
        }

        @Override
        public View getView(int i, View convertView, ViewGroup viewGroup) {
            GridHolder holder;

            if(convertView == null){
                holder = new GridHolder();

                convertView = mInflater.inflate(R.layout.layout, null);
                holder.image = (ImageView) convertView.findViewById(R.id.icon);
                holder.text = (TextView) convertView.findViewById(R.id.text);
                convertView.setTag(holder);
            }else{
                holder = (GridHolder) convertView.getTag();
            }
            PhoneMessage msg = mPhones.get(i);

            holder.text.setText(msg.info);

            if(msg.icon > 0){
                holder.image.setImageResource(msg.icon);
            } else {
                holder.image.setImageDrawable(Drawable.createFromPath(msg.icon_URL));
            }
            return convertView;
        }

        class GridHolder{
            public ImageView image;
            public TextView text;
        }
    }

    public class MyLocationListener implements BDLocationListener {
        @Override
        public void onReceiveLocation(BDLocation location) {
            if (location == null)
                return ;
            StringBuffer sb = new StringBuffer(256);
            sb.append("time : ");
            sb.append(location.getTime());
//            sb.append("\nerror code : ");
//            sb.append(location.getLocType());
            sb.append("\nlatitude : ");
            sb.append(location.getLatitude());
            sb.append("\nlontitude : ");
            sb.append(location.getLongitude());
            sb.append("\nradius : ");
            sb.append(location.getRadius());
            if (location.getLocType() == BDLocation.TypeGpsLocation){
                sb.append("\nspeed : ");
                sb.append(location.getSpeed());
                sb.append("\nsatellite : ");
                sb.append(location.getSatelliteNumber());
            } else if (location.getLocType() == BDLocation.TypeNetWorkLocation){
                sb.append("\naddr : ");
                sb.append(location.getAddrStr());
            }

            if(mAdderss.length() == 0 || ! mAdderss.equals(sb.toString())){
                mHandler.sendEmptyMessage(1);
                Log.d("SUN", sb.toString());
            }
            mAdderss = sb.toString();
        }
    }
}
