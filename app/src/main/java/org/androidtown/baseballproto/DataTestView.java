package org.androidtown.baseballproto;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * Created by Administrator on 2017-05-17.
 */

public class DataTestView extends LinearLayout {
    TextView textView;
    TextView textView2;
    TextView textView3;
    ImageView imageView;

    public DataTestView(Context context){
        super(context);
        init(context);
    }

    public DataTestView(Context context, AttributeSet attrs){
        super(context,attrs);
        init(context);
    }

    public void init(Context context){
        LayoutInflater inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.datatest_item,this,true);

        textView=(TextView)findViewById(R.id.list_textView);
        textView2=(TextView)findViewById(R.id.list_textView2);
        textView3=(TextView)findViewById(R.id.list_textView3);
        imageView=(ImageView)findViewById(R.id.list_imageView);
    }

    public void setName(String name){
        textView.setText(name);
    }

    public void setMobile(String mobile){
        textView2.setText(mobile);
    }

    public void setAge(int age){
        textView3.setText(String.valueOf(age));
    }

    public void setImage(int resId){
        imageView.setImageResource(resId);
    }
}

