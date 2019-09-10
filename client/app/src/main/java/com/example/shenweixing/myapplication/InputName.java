package com.example.shenweixing.myapplication;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.example.shenweixing.myapplication.utils.T;
import com.example.shenweixing.myapplication.utils.TextUtil;

import java.util.ArrayList;
import java.util.List;

import io.realm.RealmConfiguration;

/**
 * 诚志海图
 * Created by ShenWeiXing on 2019/9/10.10:09
 * Description:
 */

public class InputName extends AppCompatActivity {

    private EditText name;
    private Button ok;
    private List<UserMessageBean> list = new ArrayList<>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_input_name);
        name = (EditText) findViewById(R.id.name);
        ok = (Button) findViewById(R.id.ok);
        list.add(new UserMessageBean());
        RealmConfiguration config = new RealmConfiguration.Builder()
                .deleteRealmIfMigrationNeeded()
                .build();
        List<String> primiss = new ArrayList<>();
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            primiss.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
            //ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            primiss.add(Manifest.permission.READ_EXTERNAL_STORAGE);
            //ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            primiss.add(Manifest.permission.ACCESS_COARSE_LOCATION);
            //ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, 1);//自定义的code
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            primiss.add(Manifest.permission.CAMERA);
            //ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, 1);
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED) {
            primiss.add(Manifest.permission.RECORD_AUDIO);
            //ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, 1);
        }
        if (primiss.size() != 0) {
            String[] permissions = primiss.toArray(new String[primiss.size()]);
            ActivityCompat.requestPermissions(this, permissions, 2);
        }
        ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent;
//                if (!TextUtil.isEmpty(name.getText().toString())) {
                if (!"".equals(name.getText().toString())){
                    String matching = matching(name.getText().toString());
                    if(matching==null){
                        T.showLong(InputName.this,"用户不存在");
                    }else{
                        intent = new Intent(InputName.this, VoideoChatActivity.class);
                        UserMessageBean userMessageBean = new UserMessageBean();
                        userMessageBean.setIpAdress(matching);
                        intent.putExtra("data",userMessageBean);
                        intent.putExtra("type","1");
                        startActivity(intent);
                        finish();
                    }
                }else {
                    T.showLong(InputName.this,"昵称不能为空");
                }
            }
        });
    }
    private String matching(String name) {
        if(name.equals("111")){
            return "192.168.11.160";
        }
        return null;
    }

}
