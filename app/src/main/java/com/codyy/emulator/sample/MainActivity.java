package com.codyy.emulator.sample;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.codyy.emulator.detect.library.DeviceInfo;
import com.codyy.emulator.detect.library.EmulatorDetect;
import com.codyy.lib.crash.CrashMail;
import com.codyy.lib.crash.MailUtils;
import com.codyy.rx.permissions.RxPermissions;

import rx.functions.Action1;

public class MainActivity extends AppCompatActivity implements Handler.Callback {
    private Handler mHandler;
    TextView place;
    TextView textView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mHandler = new Handler(this);
        place = (TextView) findViewById(R.id.tv_placeholder);
        textView = (TextView) findViewById(R.id.tv_info);
        RxPermissions rxPermissions = new RxPermissions(getSupportFragmentManager());
        rxPermissions.request(Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_CONTACTS, Manifest.permission.READ_PHONE_STATE)
                .subscribe(new Action1<Boolean>() {
                    @Override
                    public void call(Boolean aBoolean) {
                        if (aBoolean) {
                            new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    DeviceInfo info = EmulatorDetect.emulatorDetect(MainActivity.this);
                                    Message msg = new Message();
                                    msg.obj = info;
                                    mHandler.sendMessage(msg);
                                }
                            }).start();
                        } else {
                            tip(MainActivity.this);
                        }
                    }
                });
    }


    private class SendAsyncTask extends AsyncTask<String, Integer, Boolean> {

        @Override
        protected Boolean doInBackground(String... params) {
            try {
                MailUtils.sendMail(new CrashMail("mail.codyy.cn", params[0], params[1], "android@codyy.com", "运营平台2.2.0", "android", "lijian@codyy.com", "李健"));
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
            return true;
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            super.onPostExecute(aBoolean);
            if (aBoolean) Toast.makeText(MainActivity.this, "发送成功", Toast.LENGTH_SHORT).show();
            else Toast.makeText(MainActivity.this, "发送失败", Toast.LENGTH_SHORT).show();
        }
    }

    public void tip(final Context context) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("提示").setMessage("未授予相关权限,部分功能无法使用");
        builder.setCancelable(true);
        builder.setNegativeButton("设置", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                openSettings(context);
            }
        }).create().show();
    }

    private static void openSettings(Context context) {
        Uri packageURI = Uri.parse("package:" + context.getPackageName());
        Intent intent = new Intent("android.settings.APPLICATION_DETAILS_SETTINGS", packageURI);
        context.startActivity(intent);
    }

    @Override
    public boolean handleMessage(Message msg) {
        DeviceInfo info = (DeviceInfo) msg.obj;
        textView.setText(info.getInfo() + (info.isEmulator() ? "模拟器" : "真机"));
        place.setVisibility(View.GONE);
        SendAsyncTask asyncTask = new SendAsyncTask();
        asyncTask.execute("设备信息", info.getInfo() + (info.isEmulator() ? "模拟器" : "真机"));
        return false;
    }
}
