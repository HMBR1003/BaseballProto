package org.androidtown.baseballproto;

import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.androidtown.baseballproto.databinding.ActivityBusinessSignupBinding;

public class BusinessSignupActivity extends AppCompatActivity {

    String uid;
    int value;
    ActivityBusinessSignupBinding dataBinding;
    DatabaseReference myRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        dataBinding = DataBindingUtil.setContentView(this, R.layout.activity_business_signup);

        //uid 가져오기
        final Intent intent=getIntent();
        uid = intent.getStringExtra("uid");

        //데이터베이스 초기화
        myRef = FirebaseDatabase.getInstance().getReference("users");

        //데이터베이스에서 유저가 고객인지 사업자 등록중인지 사업자인지 담는 정보를 불러옴
        myRef.child(uid).child("isBusiness(0(not),1(applying),2(finish))").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                value = dataSnapshot.getValue(Integer.class);
                Toast.makeText(BusinessSignupActivity.this, "데이터 가져오기 성공", Toast.LENGTH_SHORT).show();
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(BusinessSignupActivity.this, "데이터 가져오기 실패", Toast.LENGTH_SHORT).show();
            }
        });

        //로그인한 유저가 고객인지 사업자 등록중인지 사업자인지 검사함
        if(value==0) {      //유저가 고객이고 사업자 등록신청을 하는 경우
            Toast.makeText(this, "액션바 변경", Toast.LENGTH_SHORT).show();
            dataBinding.toolBar.setTitle("사업자 신규등록 신청");
            dataBinding.toolBar.setTitleTextColor(Color.WHITE);
            dataBinding.inputBusinessInfo.setText("매장 정보 입력");
            dataBinding.businessSubmit.setText("작성 완료");
        }
        else if(value==1) {
            dataBinding.toolBar.setTitle("사업자 신청정보 수정");
            dataBinding.toolBar.setTitleTextColor(Color.WHITE);
            dataBinding.inputBusinessInfo.setText("매장 정보 수정");
            dataBinding.businessSubmit.setText("작성 완료");
        }
        else if(value==2){
                dataBinding.toolBar.setTitle("매장 정보 수정");
                dataBinding.toolBar.setTitleTextColor(Color.WHITE);
                dataBinding.inputBusinessInfo.setText("");
                dataBinding.businessSubmit.setText("작성 완료");
        }
        else{
            finish();
        }
        //뒤로가기 버튼 만들기
        setSupportActionBar(dataBinding.toolBar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        dataBinding.businessSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

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

