package org.androidtown.baseballproto;

import android.*;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.databinding.DataBindingUtil;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.IgnoreExtraProperties;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.UploadTask;

import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import org.androidtown.baseballproto.databinding.ActivityBusinessSignupBinding;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Hashtable;

public class BusinessSignupActivity extends AppCompatActivity {
    private static final int SEARCH_ADDRESS_ACTIVITY = 10000;
    private static final int GET_MARKET_IMAGE = 7000 ;

    String email;
    String name;
    String uid;
    int isBusiness;     //사업자 구분하기 위해 선언
    ActivityBusinessSignupBinding dataBinding;  //데이터 바인딩
    DatabaseReference myRef;

    Bitmap bitmap=null;
    ProgressDialog dialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        dataBinding = DataBindingUtil.setContentView(this, R.layout.activity_business_signup);

        //주소찾기 란 입력하지 못하게 하기 위해 설정
        dataBinding.marketAddress1.setInputType(0);
        dataBinding.marketAddress1.setFocusable(false);
        dataBinding.marketAddress1.setClickable(false);

        //프로그레스 다이얼로그 동적할당
        dialog=new ProgressDialog(BusinessSignupActivity.this);

        //주소찾기 버튼 클릭 시 Daum에서 지원하는 주소찾기 창을 띄움
        dataBinding.searchAddress.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(BusinessSignupActivity.this, AddressWebViewActivity.class);
                startActivityForResult(i, SEARCH_ADDRESS_ACTIVITY);
            }
        });


        //uid 가져오기
        Intent intent = getIntent();
        uid = intent.getStringExtra("uid");
        isBusiness = intent.getIntExtra("isBusiness", -1);
        email = intent.getStringExtra("email");
        name = intent.getStringExtra("name");

        dataBinding.loadImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(i, GET_MARKET_IMAGE);
            }
        });



        //유저가 고객이고 사업자 등록신청을 하는 경우 화면 설정
        if (isBusiness == 0) {
            dataBinding.toolBar.setTitle("사업자 신규등록 신청");
            dataBinding.toolBar.setTitleTextColor(Color.WHITE);
            dataBinding.inputBusinessInfo.setText("사업자로 신청할 정보를 입력해 주세요");
            dataBinding.businessSubmit.setText("작성 완료");
        }

        //사업자 등록신청 승인을 기다리는 고객의 경우 화면 설정(임시 데이터베이스에 저장된 내용을 불러와서 세팅함)
        else if (isBusiness == 1) {
            dataBinding.toolBar.setTitle("사업자 신청정보 수정");
            dataBinding.toolBar.setTitleTextColor(Color.WHITE);
            dataBinding.inputBusinessInfo.setText("신청한 정보를 수정합니다.");
            dataBinding.businessSubmit.setText("수정 완료");

            //데이터베이스에 저장된 데이터 가져오기 함수
            loadData("tmp");
        }

        //사업자 승인이 난 고객의 경우 화면 설정(확정 데이터베이스에 저장된 내용을 불러와서 세팅함)
        else if (isBusiness == 2) {
            dataBinding.toolBar.setTitle("매장 정보 수정");
            dataBinding.toolBar.setTitleTextColor(Color.WHITE);
            dataBinding.inputBusinessInfo.setText("등록된 사업자 정보를 수정합니다.");
            dataBinding.businessSubmit.setText("수정 완료");

            loadData("market");
        }

        //인텐트 값이 제대로 넘어오지 않았을 경우
        else {
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
                if (textCheck()) {
                    submitAlert();
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
        if(dataBinding.marketAddress1.length()<=0){
            Toast.makeText(this, "주소를 검색하여 주세요.", Toast.LENGTH_SHORT).show();
            return false;
        }
        if(dataBinding.marketAddress2.length()<=0){
            Toast.makeText(this, "상세 주소를 입력해주세요.", Toast.LENGTH_SHORT).show();
            return false;
        }
        if(dataBinding.marketTel.length()<=0){
            Toast.makeText(this, "매장 전화번호를 입력해주세요.", Toast.LENGTH_SHORT).show();
            return false;
        }
        if(bitmap==null){
            Toast.makeText(this, "매장 사진을 등록해주세요.", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }
    //유저에게 최종확인후 데이터 넣는 작업을 하는 함수
    public void submitAlert(){

        //확인창 만들기
        AlertDialog.Builder builder = new AlertDialog.Builder(BusinessSignupActivity.this);
        builder.setTitle("확인창");
        builder.setMessage("이대로 제출하시겠습니까?");
        //확인 버튼설정 및 버튼을 눌렀을 때 동작 설정
        builder.setPositiveButton("확인", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                //고객이 사업자 등록 신청을 하는경우와 신청 중인 고객이 수정을 하는 경우, 임시 데이터베이스로 입력된 정보를 넣는다.
                if(isBusiness==0||isBusiness==1) {
                    //데이터베이스 초기화
                    myRef = FirebaseDatabase.getInstance().getReference();
                    myRef.child("tmp").child(uid).child("accountEmail").setValue(email);
                    myRef.child("tmp").child(uid).child("accountName").setValue(name);

                    myRef.child("tmp").child(uid).child("manName").setValue(dataBinding.manName.getText().toString());
                    myRef.child("tmp").child(uid).child("manTel").setValue(dataBinding.manTel.getText().toString());
                    myRef.child("tmp").child(uid).child("businessRegisterNum").setValue(dataBinding.businessRegisterNum.getText().toString());
                    myRef.child("tmp").child(uid).child("marketName").setValue(dataBinding.marketName.getText().toString());
                    switch (dataBinding.handleFoodRadio.getCheckedRadioButtonId()) {
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
                    myRef.child("tmp").child(uid).child("marketAddress1").setValue(dataBinding.marketAddress1.getText().toString());
                    myRef.child("tmp").child(uid).child("marketAddress2").setValue(dataBinding.marketAddress2.getText().toString());
                    myRef.child("tmp").child(uid).child("marketTel").setValue(dataBinding.marketTel.getText().toString());
                    myRef.child("users").child(uid).child("isBusiness(0(not),1(applying),2(finish))").setValue(1);
                    uploadImage();
                }

                //사장 고객이 매장 정보를 수정하는 경우
                else  if(isBusiness==3){
                    //데이터베이스 초기화
                    myRef = FirebaseDatabase.getInstance().getReference();
                    myRef.child("market").child(uid).child("accountEmail").setValue(email);
                    myRef.child("market").child(uid).child("accountName").setValue(name);

                    myRef.child("market").child(uid).child("manName").setValue(dataBinding.manName.getText().toString());
                    myRef.child("market").child(uid).child("manTel").setValue(dataBinding.manTel.getText().toString());
                    myRef.child("market").child(uid).child("businessRegisterNum").setValue(dataBinding.businessRegisterNum.getText().toString());
                    myRef.child("market").child(uid).child("marketName").setValue(dataBinding.marketName.getText().toString());
                    switch (dataBinding.handleFoodRadio.getCheckedRadioButtonId()) {
                        case R.id.radioChicken:
                            myRef.child("market").child(uid).child("handleFood").setValue(1);
                            break;
                        case R.id.radioPizza:
                            myRef.child("market").child(uid).child("handleFood").setValue(2);
                            break;
                        case R.id.radioBurger:
                            myRef.child("market").child(uid).child("handleFood").setValue(3);
                            break;
                        case R.id.radioPig:
                            myRef.child("market").child(uid).child("handleFood").setValue(4);
                            break;
                        case R.id.radioTake:
                            myRef.child("market").child(uid).child("handleFood").setValue(5);
                            break;
                        case R.id.radioEtc:
                            myRef.child("market").child(uid).child("handleFood").setValue(6);
                            break;
                        default:
                            myRef.child("market").child(uid).child("handleFood").setValue(0);
                            break;
                    }
                    myRef.child("market").child(uid).child("marketAddress1").setValue(dataBinding.marketAddress1.getText().toString());
                    myRef.child("market").child(uid).child("marketAddress2").setValue(dataBinding.marketAddress2.getText().toString());
                    myRef.child("market").child(uid).child("marketTel").setValue(dataBinding.marketTel.getText().toString());
                    myRef.child("users").child(uid).child("isBusiness(0(not),1(applying),2(finish))").setValue(2);
                    uploadImage();
                }
                else{
                    Toast.makeText(BusinessSignupActivity.this, "데이터 저장 시 사업자 여부 데이터 오류", Toast.LENGTH_SHORT).show();
                }
            }
        });
        //닫기
        builder.setNegativeButton("취소", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        AlertDialog dialog = builder.create();
        dialog.setCancelable(false);
        dialog.show();
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
                        finish();
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

    //주소찾기나 사진 불러오기 결과를 받아왔을 때의 동작 설정
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        //주소찾기 화면에서 결과를 받아왔을 경우
        if(requestCode==SEARCH_ADDRESS_ACTIVITY&&resultCode==RESULT_OK){
            dataBinding.marketAddress1.setText(data.getStringExtra("data"));   //인텐트로 받아온 주소값을 텍스트에 설정한다
        }
        //매장 대표사진 설정에서 결과를 받아왔을 경우
        else if(requestCode==GET_MARKET_IMAGE&&resultCode==RESULT_OK) {
            Uri image = data.getData(); //인텐트에서 이미지에 대한 데이터 추출
            try {
                //이미지를 비트맵 변수로 저장
                bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), image);
                //이미지뷰에 비트맵 변수를 세팅
                dataBinding.marketImageView.setImageBitmap(bitmap);

                //예외 처리 문장
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    //데이터베이스에서 데이터를 로드해서 세팅해주는 함수
    public  void loadData(String s){
        //데이터 불러오는 중이라고 알림창 띄우기
        dialog.setProgress(ProgressDialog.STYLE_SPINNER);
        dialog.setMessage("데이터를 불러오는 중입니다...");
        dialog.setCancelable(false);
        dialog.setOnKeyListener(new Dialog.OnKeyListener() {
            @Override
            public boolean onKey(DialogInterface arg0, int keyCode,
                                 KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_BACK) {
                    dialog.dismiss();
                    finish();
                }
                return true;
            }
        });
        dialog.show();

        //데이터베이스 초기화
        myRef = FirebaseDatabase.getInstance().getReference();

        //데이터 불러와서 화면에 세팅하기
        myRef.child(s).child(uid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue() != null) {
                    MarketInfo data = dataSnapshot.getValue(MarketInfo.class);
                    dataBinding.manName.setText(data.manName);
                    dataBinding.businessRegisterNum.setText(data.businessRegisterNum);
                    dataBinding.manTel.setText(data.manTel);
                    dataBinding.marketName.setText(data.marketName);
                    switch ((int) data.handleFood) {
                        case 1:
                            dataBinding.handleFoodRadio.check(R.id.radioChicken);
                            break;
                        case 2:
                            dataBinding.handleFoodRadio.check(R.id.radioPizza);
                            break;
                        case 3:
                            dataBinding.handleFoodRadio.check(R.id.radioBurger);
                            break;
                        case 4:
                            dataBinding.handleFoodRadio.check(R.id.radioPig);
                            break;
                        case 5:
                            dataBinding.handleFoodRadio.check(R.id.radioTake);
                            break;
                        case 6:
                            dataBinding.handleFoodRadio.check(R.id.radioEtc);
                            break;
                        default:
                            dataBinding.handleFoodRadio.clearCheck();
                            break;
                    }
                    dataBinding.marketAddress1.setText(data.marketAddress1);
                    dataBinding.marketAddress2.setText(data.marketAddress2);
                    dataBinding.marketTel.setText(data.marketTel);

                    //피카소를 이용하여 저장소에 저장된 사진을 url로 이미지뷰에 연결하기
                    Picasso.with(getApplicationContext())
                            .load(data.marketImageUrl)
                            .fit()
                            .centerInside()
                            .into(dataBinding.marketImageView, new Callback.EmptyCallback() {
                                @Override
                                public void onSuccess() {
                                    BitmapDrawable d = (BitmapDrawable) dataBinding.marketImageView.getDrawable();
                                    bitmap = d.getBitmap();
                                    dialog.dismiss();
                                }
                            });


//                    Toast.makeText(BusinessSignupActivity.this, "데이터 가져오기 성공", Toast.LENGTH_SHORT).show();
                }
                else{
                    dialog.dismiss();
                    Toast.makeText(BusinessSignupActivity.this, "불러올 데이터가 없습니다.", Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
                dialog.dismiss();
                Toast.makeText(BusinessSignupActivity.this, "데이터 가져오기 실패 에러 내용 : "+databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

    }

    public void uploadImage(){
        //데이터 저장하는 중이라고 알림창 띄우기
        dialog.setProgress(ProgressDialog.STYLE_SPINNER);
        dialog.setMessage("데이터를 저장하는 중입니다...");
        dialog.setCancelable(false);
        dialog.show();

        //저장소에 대한 참조 만들기
        StorageReference  mStorageRef = FirebaseStorage.getInstance().getReference();
        //실제로 이미지가 저장될 곳의 참조
        StorageReference mountainsRef = mStorageRef.child("tmp").child(uid).child("market.jpg");

        //비트맵을 jpg로 변환시켜서 변수에 저장
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] data = baos.toByteArray();

        //jpg형식으로 저장된 변수를 저장소에 업로드하는 함수
        UploadTask uploadTask = mountainsRef.putBytes(data);
        //성공했을 시와 실패했을 시를 받아오는 리스너 부착
        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                dialog.dismiss();
                Toast.makeText(BusinessSignupActivity.this, "제출 실패.", Toast.LENGTH_SHORT).show();
                // Handle unsuccessful uploads
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @SuppressWarnings("VisibleForTests")
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                // taskSnapshot.getMetadata() contains file metadata such as size, content-type, and download URL.
                Uri downloadUrl = taskSnapshot.getDownloadUrl();
                String photoUri =  String.valueOf(downloadUrl);
                myRef.child("tmp").child(uid).child("marketImageUrl").setValue(photoUri);
                dialog.dismiss();
                Toast.makeText(BusinessSignupActivity.this, "제출 완료.", Toast.LENGTH_SHORT).show();
                setResult(RESULT_OK);
                finish();
            }
        });
    }
}

