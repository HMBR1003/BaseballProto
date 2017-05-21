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
    int isBusiness;
    ActivityBusinessSignupBinding dataBinding;
    DatabaseReference myRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        dataBinding = DataBindingUtil.setContentView(this, R.layout.activity_business_signup);

        //uid 가져오기
        final Intent intent=getIntent();
        uid = intent.getStringExtra("uid");
        isBusiness=intent.getIntExtra("isBusiness",-1);

        //데이터베이스 초기화
        myRef = FirebaseDatabase.getInstance().getReference("users");

        //데이터베이스에서 유저가 고객인지 사업자 등록중인지 사업자인지 담는 정보를 불러옴

        //로그인한 유저가 고객인지 사업자 등록중인지 사업자인지 검사함

        //유저가 고객이고 사업자 등록신청을 하는 경우 화면 설정
        if(isBusiness==0) {
            dataBinding.toolBar.setTitle("사업자 신규등록 신청");
            dataBinding.toolBar.setTitleTextColor(Color.WHITE);
            dataBinding.inputBusinessInfo.setText("사업자로 신청할 정보를 입력해 주세요");
            dataBinding.businessSubmit.setText("작성 완료");
        }

        //사업자 등록신청 승인을 기다리는 고객의 경우 화면 설정(임시 데이터베이스에 저장된 내용을 불러와서 세팅함)
        else if(isBusiness==1) {
            dataBinding.toolBar.setTitle("사업자 신청정보 수정");
            dataBinding.toolBar.setTitleTextColor(Color.WHITE);
            dataBinding.inputBusinessInfo.setText("신청할 정보를 수정합니다.");
            dataBinding.businessSubmit.setText("수정 완료");
        }

        //사업자 승인이 난 고객의 경우 화면 설정(확정 데이터베이스에 저장된 내용을 불러와서 세팅함)
        else if(isBusiness==2){
                dataBinding.toolBar.setTitle("매장 정보 수정");
                dataBinding.toolBar.setTitleTextColor(Color.WHITE);
                dataBinding.inputBusinessInfo.setText("등록된 사업자 정보를 수정합니다.");
                dataBinding.businessSubmit.setText("수정 완료");
        }

        //인텐트 값이 제대로 넘어오지 않았을 경우
        else{
            Toast.makeText(this, "사업자 여부 불러오기 오류", Toast.LENGTH_SHORT).show();
            finish();
        }



        //뒤로가기 버튼 만들기
        setSupportActionBar(dataBinding.toolBar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //하단의 완료 버튼을 눌렀을 때의 동작 설정
        dataBinding.businessSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //고객이 사업자 등록 신청을 하는경우, 임시 데이터베이스로 입력된 정보를 넣는다.
                if(isBusiness==0){

                }
                //등록 신청 중인 고객이 수정을 할 경우, 마찬가지로 임시 데이터베이스로 입력된 정보를 넣는다.
                else if(isBusiness==1){

                }

                //사업자 고객이 수정을 할 경우엔 확정 데이터베이스에 입력된 정보를 넣는다.
                else if(isBusiness==2){

                }
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

