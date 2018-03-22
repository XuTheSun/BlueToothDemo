package com.example.bluetoothdemo;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.TextView;

public class BlueToothStates extends AppCompatActivity {

    private TextView tv_name;
    private TextView tv_address;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_blue_tooth_states);
        tv_address=findViewById(R.id.textView_address);
        tv_name=findViewById(R.id.textView_name);

        Intent intent=getIntent();
        Bundle bundle=intent.getExtras();
        String name=bundle.get("name").toString();
        String address=bundle.get("address").toString();
        tv_name.setText(tv_name.getText().toString()+" "+name);
        tv_address.setText(tv_address.getText().toString()+" "+ address);


    }

}
