package org.androidtown.baseballproto;

import android.databinding.DataBindingUtil;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;

import org.androidtown.baseballproto.databinding.ActivityBusinessSignupBinding;

public class BusinessSignupActivity extends AppCompatActivity {
        ActivityBusinessSignupBinding dataBinding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        dataBinding = DataBindingUtil.setContentView(this, R.layout.activity_business_signup);

        //뒤로가기 버튼 만들기
        dataBinding.toolBar.setTitle("사업자 신규등록 신청");         //타이틀입니다.
        dataBinding.toolBar.setTitleTextColor(Color.WHITE);
        setSupportActionBar(dataBinding.toolBar);
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
