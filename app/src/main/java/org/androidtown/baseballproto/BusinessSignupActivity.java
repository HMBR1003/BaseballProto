package org.androidtown.baseballproto;

import android.*;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.databinding.DataBindingUtil;
import android.graphics.Color;
import android.net.Uri;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.androidtown.baseballproto.databinding.ActivityBusinessSignupBinding;

public class BusinessSignupActivity extends AppCompatActivity {
    private static final int SEARCH_ADDRESS_ACTIVITY = 10000;

    String uid;
    int isBusiness;     //사업자 구분하기 위해 선언
    ActivityBusinessSignupBinding dataBinding;  //데이터 바인딩
    DatabaseReference myRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        dataBinding = DataBindingUtil.setContentView(this, R.layout.activity_business_signup);

        //주소찾기 란 입력하지 못하게 하기 위해 설정
        dataBinding.addressText.setInputType(0);
        dataBinding.addressText.setFocusable(false);
        dataBinding.addressText.setClickable(false);

        //주소찾기 버튼 클릭 시 다음에서 지원하는 주소찾기 창을 띄움
        dataBinding.searchAddress.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(BusinessSignupActivity.this, AddressWebViewActivity.class);
                startActivityForResult(i, SEARCH_ADDRESS_ACTIVITY);
            }
        });


        //uid 가져오기
        Intent intent=getIntent();
        uid = intent.getStringExtra("uid");
        isBusiness=intent.getIntExtra("isBusiness",-1);



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
                    if(textCheck()){
                        //데이터베이스 초기화
                        myRef = FirebaseDatabase.getInstance().getReference();
                        myRef.child("tmp").child(uid).child("userName").setValue(dataBinding.manName.getText().toString());
                        myRef.child("tmp").child(uid).child("userTel").setValue(dataBinding.manTel.getText().toString());
                        myRef.child("tmp").child(uid).child("marketName").setValue(dataBinding.marketName.getText().toString());
                        switch(dataBinding.handleRadio.getCheckedRadioButtonId()){
                            case R.id.radioChicken:
                                myRef.child("tmp").child(uid).child("handleFood").setValue(1);
                                break;
                            case R.id.radioPizza:
                                myRef.child("tmp").child(uid).child("handleFood").setValue(2);
                                break;
                            case R.id.radioBurger:
                                myRef.child("tmp").child(uid).child("handleFood").setValue(3);
                                break;
                            case R.id.radioPig:
                                myRef.child("tmp").child(uid).child("handleFood").setValue(4);
                                break;
                            case R.id.radioTake:
                                myRef.child("tmp").child(uid).child("handleFood").setValue(5);
                                break;
                            case R.id.radioEtc:
                                myRef.child("tmp").child(uid).child("handleFood").setValue(6);
                                break;
                            default:
                                myRef.child("tmp").child(uid).child("handleFood").setValue(0);
                                break;
                        }
                        myRef.child("tmp").child(uid).child("marketAddress").setValue(dataBinding.addressText.getText().toString() + " " + dataBinding.marketAddress.getText().toString());
                        myRef.child("tmp").child(uid).child("marketTel").setValue(dataBinding.marketTel.getText().toString());
                        myRef.child("users").child(uid).child("isBusiness(0(not),1(applying),2(finish))").setValue(1);
                        Toast.makeText(BusinessSignupActivity.this, "신청 되었습니다.", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                }
                //등록 신청 중인 고객이 수정을 할 경우, 마찬가지로 임시 데이터베이스로 입력된 정보를 넣는다.
                else if(isBusiness==1){
                    if(textCheck()){
                        //데이터베이스 초기화
                        myRef = FirebaseDatabase.getInstance().getReference();
                        myRef.child("tmp").child(uid).child("userName").setValue(dataBinding.manName.getText().toString());
                        myRef.child("tmp").child(uid).child("userTel").setValue(dataBinding.manTel.getText().toString());
                        myRef.child("tmp").child(uid).child("marketName").setValue(dataBinding.marketName.getText().toString());
                        switch(dataBinding.handleRadio.getCheckedRadioButtonId()){
                            case R.id.radioChicken:
                                myRef.child("tmp").child(uid).child("handleFood").setValue(1);
                                break;
                            case R.id.radioPizza:
                                myRef.child("tmp").child(uid).child("handleFood").setValue(2);
                                break;
                            case R.id.radioBurger:
                                myRef.child("tmp").child(uid).child("handleFood").setValue(3);
                                break;
                            case R.id.radioPig:
                                myRef.child("tmp").child(uid).child("handleFood").setValue(4);
                                break;
                            case R.id.radioTake:
                                myRef.child("tmp").child(uid).child("handleFood").setValue(5);
                                break;
                            case R.id.radioEtc:
                                myRef.child("tmp").child(uid).child("handleFood").setValue(6);
                                break;
                            default:
                                myRef.child("tmp").child(uid).child("handleFood").setValue(0);
                                break;
                            }
                        myRef.child("tmp").child(uid).child("marketAddress").setValue(dataBinding.addressText.getText().toString() + " " + dataBinding.marketAddress.getText().toString());
                        myRef.child("tmp").child(uid).child("marketTel").setValue(dataBinding.marketTel.getText().toString());
                        //하위 종목 추가 테스트
//                        myRef.child("tmp").child(uid).child("menu").child("테스트").setValue("메뉴");
                        Toast.makeText(BusinessSignupActivity.this, "수정 되었습니다.", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                }

                //사업자 고객이 수정을 할 경우엔 확정 데이터베이스에 입력된 정보를 수정한다.
                else if(isBusiness==2){
                    if(textCheck()){

                    }
                }
            }
        });

    }

    public boolean textCheck(){
        if(dataBinding.manName.length()<=0){
            Toast.makeText(this, "사업자명을 입력해주세요.", Toast.LENGTH_SHORT).show();
            return false;
        }
        if(dataBinding.manTel.length()<=0){
            Toast.makeText(this, "사업자 전화번호를 입력해주세요.", Toast.LENGTH_SHORT).show();
            return false;
        }
        if(dataBinding.marketName.length()<=0){
            Toast.makeText(this, "매장명을 입력해주세요.", Toast.LENGTH_SHORT).show();
            return false;
        }
        if(!(dataBinding.radioBurger.isChecked()||
                dataBinding.radioChicken.isChecked()||
                dataBinding.radioPig.isChecked()||
                dataBinding.radioPizza.isChecked()||
                dataBinding.radioTake.isChecked()||
                dataBinding.radioEtc.isChecked())){
            Toast.makeText(this, "취급 음식을 체크 해주세요", Toast.LENGTH_SHORT).show();
            return false;
        }
        if(dataBinding.addressText.length()<=0){
            Toast.makeText(this, "주소를 검색하여 주세요.", Toast.LENGTH_SHORT).show();
            return false;
        }
        if(dataBinding.marketAddress.length()<=0){
            Toast.makeText(this, "상세 주소를 입력해주세요.", Toast.LENGTH_SHORT).show();
            return false;
        }
        if(dataBinding.marketTel.length()<=0){
            Toast.makeText(this, "매장 전화번호를 입력해주세요.", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
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

    //저장소 접근권한 묻기 설정
    @Override
    protected void onResume() {
        super.onResume();
        int permissionCheck = ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_EXTERNAL_STORAGE);
        if(permissionCheck== PackageManager.PERMISSION_DENIED) {

            //사용자가 권한을 한번 이라도 거부 했던 경우
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, android.Manifest.permission.READ_EXTERNAL_STORAGE)) {
                //알림창을 띄운다
                AlertDialog.Builder builder = new AlertDialog.Builder(BusinessSignupActivity.this);
                builder.setTitle("알림");
                builder.setMessage("매장 사진을 업로드 하기 위해 저장소 권한을 허용해주세요.");

                //앱 설정으로 이동하는 버튼
                builder.setPositiveButton("설정으로 이동", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent i = new Intent();
                        i.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                        i.addCategory(Intent.CATEGORY_DEFAULT);
                        i.setData(Uri.parse("package:" + getApplication().getPackageName()));
                        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        i.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                        i.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
                        startActivityForResult(i, 2);
                    }
                });
                //닫기
                builder.setNegativeButton("닫기", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                });
                AlertDialog dialog = builder.create();
                dialog.setCancelable(false);
                dialog.show();
            //처음 권한을 묻는 경우
            } else {
                ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
            }
        }
    }

    //권한요청 후 결과를 받았을 때 실행되는 메소드
    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 1:
            case 2:
                //사용자가 권한을 허가했을 때
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    //사용자가 권한을 거부했을 때
                } else {
                    AlertDialog.Builder builder = new AlertDialog.Builder(BusinessSignupActivity.this);
                    builder.setTitle("알림");
                    builder.setMessage("저장소 권한을 허용해주세요.");

                    builder.setPositiveButton("설정으로 이동", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Intent i = new Intent();
                            i.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                            i.addCategory(Intent.CATEGORY_DEFAULT);
                            i.setData(Uri.parse("package:" + getApplication().getPackageName()));
                            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            i.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                            i.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
                            startActivityForResult(i, 2);
                        }
                    });

                    //닫기
                    builder.setNegativeButton("닫기", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            finish();
                        }
                    });
                    AlertDialog dialog = builder.create();
                    dialog.setCancelable(false);
                    dialog.show();
                }
                return;
        }
    }

    //주소찾기 실행 후 결과를 받아왔을 때의 동작 설정
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==SEARCH_ADDRESS_ACTIVITY&&resultCode==RESULT_OK){
            dataBinding.addressText.setText(data.getStringExtra("data"));   //인텐트로 받아온 주소값을 텍스트에 설정한다
        }
    }
}

