package com.meizu.zhangxing.chargebattery;

import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;

public class ChargeBattery extends AppCompatActivity {
    private static final String TAG = "ChargeBattery";

    public static final int MSG_UPDATE = 1;

    private static final String PATH = "/sys/class/meizu/charger/vbus_voltage";

    private TextView mInfo = null;
    private boolean mRun = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_charge_battery);
        mInfo = (TextView)findViewById(R.id.battery_charge_info_text);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mRun = false;
    }

    @Override
    protected void onResume() {
        super.onResume();
        mRun = true;
        new GetVoltageThread().start();
    }

    public Handler mUpdateHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case MSG_UPDATE:
                    Bundle b = msg.getData();
                    mInfo.setText("ADC_Charger_Voltage : " + b.get("VOLTAGE").toString() + "mV");
                    break;
                default:
                    break;
            }
        }
    };

    class GetVoltageThread extends Thread {
        @Override
        public void run() {
            while (mRun) {
                int voltage = getInfo(PATH);
                Bundle b = new Bundle();
                b.putInt("VOLTAGE", voltage);
                Message msg = mUpdateHandler.obtainMessage();
                msg.what = MSG_UPDATE;
                msg.setData(b);
                mUpdateHandler.sendMessage(msg);

                try {
                    sleep(500);
                }catch (InterruptedException e){
                    Log.d(TAG, "GetVoltageThread Interrupted!");
                }
            }
        }
    }
    public int getInfo(String Path){
        File voltage = new File(Path);
        int result = 0;
        try{
            DataInputStream in = new DataInputStream(new FileInputStream(voltage));
            result = in.readInt();
        }catch (IOException e){
            Log.d(TAG,"exception occur!");
        }
        return result;
    }
}
