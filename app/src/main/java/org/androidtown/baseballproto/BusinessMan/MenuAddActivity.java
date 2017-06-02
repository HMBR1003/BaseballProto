package org.androidtown.baseballproto.BusinessMan;

import android.databinding.DataBindingUtil;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;

import org.androidtown.baseballproto.R;
import org.androidtown.baseballproto.databinding.ActivityMenuAddBinding;

public class MenuAddActivity extends AppCompatActivity {

    ActivityMenuAddBinding binding;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_menu_add);
        //타이틀 설정
        binding.toolBar.setTitle("메뉴 추가");
        binding.toolBar.setTitleTextColor(Color.WHITE);
        setSupportActionBar(binding.toolBar);
        //뒤로가기 버튼 만들기
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }
    //뒤로가기 버튼 기능 설정
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
