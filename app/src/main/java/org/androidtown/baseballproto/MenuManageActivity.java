package org.androidtown.baseballproto;

import android.databinding.DataBindingUtil;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import org.androidtown.baseballproto.databinding.ActivityMenuManageBinding;

public class MenuManageActivity extends AppCompatActivity {

    ActivityMenuManageBinding binding;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_menu_manage);
    }
}
