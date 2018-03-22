package com.example.bluetoothdemo;

import android.app.AlertDialog;
import android.content.Context;
import android.text.Layout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Created by 关旭 on 2018/3/13.
 */

public class PreloadListAdapter extends BaseAdapter{

    private String[] names;
    private Context environment;
    private AlertDialog dialog;

    public PreloadListAdapter(Context context, String[] names, AlertDialog dialog){
        this.names = names;
        environment = context;
        this.dialog = dialog;
    }

    @Override
    public int getCount() {
        if(names == null)
            return 0;
        return names.length;
    }

    @Override
    public Object getItem(int i) {
        return names[i];
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        View view1 = LayoutInflater.from(environment).inflate(R.layout.preaload_items, null);
        TextView preloadText = view1.findViewById(R.id.preload_text);
        Button deleteBtn = view1.findViewById(R.id.preload_delete);
        ViewGroup group = (ViewGroup)preloadText.getParent();
//        int layoutWidth = group.getWidth();
//        Log.d("Debug","Width:  "+layoutWidth);
//        LinearLayout.LayoutParams params1 = new LinearLayout.LayoutParams((int)(layoutWidth * 0.875f), 100);
//        preloadText.setLayoutParams(params1);
//        LinearLayout.LayoutParams params2 = new LinearLayout.LayoutParams((int)(layoutWidth * 0.125f),100);
//        deleteBtn.setLayoutParams(params2);
        final ControlActivity controlActivity = (ControlActivity) environment;
        final int j = i;

        preloadText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(controlActivity.updateUi(j))
                    Log.d("Debug","controlActivity.updateUi 执行成功");
                else
                    Log.d("Debug","controlActivity.updateUi 执行失败");
                dialog.dismiss();

            }
        });

        deleteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(controlActivity.deleteItem(j)){
                    Log.d("Debug","controlActivity.deleteItem 执行成功");
                    ((ControlActivity) environment).runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(environment,"成功删除 "+names[j],Toast.LENGTH_SHORT).show();
                        }
                    });
                }
                else
                    Log.d("Debug","controlActivity.deleteItem 执行失败");
                dialog.dismiss();
            }
        });

        preloadText.setText(names[i]);
        return view1;
    }
}
