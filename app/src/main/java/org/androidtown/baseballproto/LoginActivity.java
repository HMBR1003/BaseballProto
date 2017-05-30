package org.androidtown.baseballproto;

import android.app.ProgressDialog;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;

import org.androidtown.baseballproto.databinding.ActivityLoginBinding;

import butterknife.BindView;

/**
 * Created by Administrator on 2017-05-13-013.
 */

public class LoginActivity extends AppCompatActivity implements
        GoogleApiClient.OnConnectionFailedListener {
    public static final int GOOGLE_SIGN_IN = 9001;
    GoogleApiClient mGoogleApiClient;  //구글 로그인 관련
    private CallbackManager mCallbackManager;   //페이스북 로그인 관련

    private FirebaseAuth mAuth;         //파이어베이스 계정 관련
    FirebaseUser user;
//    private FirebaseDatabase database;  //파이어베이스 DB 관련
    private DatabaseReference myRef;    //파이어베이스 DB 관련
    ActivityLoginBinding activityLoginBinding;  //데이터 바인딩
    String uid;
    Intent intent;

    ProgressDialog dialog;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FacebookSdk.sdkInitialize(getApplicationContext());     //페이스북 SDK 연동
        activityLoginBinding = DataBindingUtil.setContentView(this,R.layout.activity_login);    //데이터바인딩

        //타이틀 설정
        activityLoginBinding.toolBar.setTitle("로그인");
        activityLoginBinding.toolBar.setTitleTextColor(Color.WHITE);
        setSupportActionBar(activityLoginBinding.toolBar);
        //뒤로가기 버튼 만들기
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //파이어베이스 인증 객체 가져오기
        mAuth = FirebaseAuth.getInstance();
        // DB 관련 변수 초기화
//        database = FirebaseDatabase.getInstance();
        //데이터베이스 참조 객체 가져오기
        myRef = FirebaseDatabase.getInstance().getReference();

        //구글 로그인 API 관련 작업
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken("886923317230-ksfdurhset6ak2hm20t8hjcad96oe0k3.apps.googleusercontent.com")
                .requestEmail()
                .build();
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this /* FragmentActivity */, this /* OnConnectionFailedListener */)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();
        //구글 로그인 버튼 클릭 시 동작
        findViewById(R.id.button_google_login).setOnClickListener(new View.OnClickListener() {   //구글 로그인 버튼을 클릭 했을 때의 동작 설정
            @Override
            public void onClick(View v) {
                Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
                startActivityForResult(signInIntent, GOOGLE_SIGN_IN);       //구글 로그인 연동 액티비티를 결과를 받는 형식으로 띄움
            }
        });


        //페이스북 로그인 관련 작업
        mCallbackManager = CallbackManager.Factory.create();
        LoginButton facebookLoginButton = (LoginButton) findViewById(R.id.button_facebook_login);
        facebookLoginButton.setReadPermissions("email", "public_profile");  //사용자에게서 가져올 정보 권한 설정
        facebookLoginButton.registerCallback(mCallbackManager, new FacebookCallback<LoginResult>() {
            //페이스북 로그인을 성공했을 시
            @Override
            public void onSuccess(LoginResult loginResult) {
//                Toast.makeText(LoginActivity.this, "페이스북 계정 연결 성공", Toast.LENGTH_SHORT).show();
                dialog=new ProgressDialog(LoginActivity.this);
                dialog.setProgress(ProgressDialog.STYLE_SPINNER);
                dialog.setMessage("로그인 중입니다...");
                dialog.setCancelable(false);
                dialog.show();
                handleFacebookAccessToken(loginResult.getAccessToken());
            }
            @Override
            public void onCancel() {
                Toast.makeText(LoginActivity.this, "페이스북 로그인 취소", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(FacebookException e) {
                Toast.makeText(LoginActivity.this, "페이스북 로그인 에러, 에러 내용 : "+e, Toast.LENGTH_SHORT).show();
            }
        });
    }

    //페이스북 계정을 파이어베이스 인증에 등록하는 함수
    private void handleFacebookAccessToken(AccessToken token) {
        // [START_EXCLUDE silent]
        // [END_EXCLUDE]
        AuthCredential credential = FacebookAuthProvider.getCredential(token.getToken());
        mAuth.signInWithCredential(credential).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {

                        if (task.isSuccessful()) {  //등록 성공했을 시
                            // Sign in success, update UI with the signed-in user's information
                            Log.d("파이어베이스 페이스북 계정","등록 성공");

                            user = mAuth.getCurrentUser();    //유저 정보를 가져와서

                            if(user.getEmail()==null) {                                         //이메일을 허용했는 지 검사한 후
                                Toast.makeText(getApplicationContext(), "이메일을 허용하시고 다시 로그인해주세요.", Toast.LENGTH_SHORT).show();
                                user.delete()                                                   //안했으면 등록했던 계정을 삭제한다.
                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if (task.isSuccessful()) {
                                                    Log.d("파이어 베이스 페이스북 계정", "삭제");
                                                }
                                            }
                                        });
                                mAuth.signOut();                                                //그 후 파이어베이스 로그아웃
                                LoginManager.getInstance().logOut();                            //페이스북 로그아웃 한다 유저는 로그인을 다시해야한다.
                            }
                            else {
                                firebaseSignin();
                            }
                        } else {
                            // 계정 등록 실패 시
                            Log.w("파이어 베이스 페이스북 계정", "등록 실패", task.getException());
                            Toast.makeText(LoginActivity.this, "계정 등록 실패",
                                    Toast.LENGTH_SHORT).show();
                        }

                        // [START_EXCLUDE]
                        // [END_EXCLUDE]
                    }
                });
    }

    //구글 계정을 파이어베이스 인증에 등록하는 함수
    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            Log.d("구글 로그인", "파이어베이스 연동 성공");
                            //파이어베이스로 로그인
                            firebaseSignin();
                        } else {
                            Log.w("구글 로그인", "파이어베이스 연동 실패", task.getException());
                            Toast.makeText(LoginActivity.this, "로그인 실패",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    //새로 띄운 액티비티에서 결과를 받아온 경우의 동작을 설정
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        //구글 인증 액티비티를 띄우고 응답을 받아온 경우
        if (requestCode == GOOGLE_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            if (result.isSuccess()) {
                GoogleSignInAccount acct = result.getSignInAccount();
//                Toast.makeText(LoginActivity.this, "구글 계정 연결 성공", Toast.LENGTH_SHORT).show();
                dialog=new ProgressDialog(this);
                dialog.setProgress(ProgressDialog.STYLE_SPINNER);
                dialog.setMessage("로그인 중입니다...");
                dialog.setCancelable(false);
                dialog.show();
                firebaseAuthWithGoogle(acct);
            } else {
                // Signed out, show unauthenticated UI.
                Toast.makeText(LoginActivity.this, "구글 계정 연결 실패", Toast.LENGTH_SHORT).show();
            }
        }
        else {                  //구글이 아닐 경우엔 페이스북에 전달한다.
            // Pass the activity result back to the Facebook SDK
            mCallbackManager.onActivityResult(requestCode, resultCode, data);
        }
    }
    //파이어베이스 로그인 연결 함수
    public void firebaseSignin(){
        //이메일을 제공했을 시 데이터베이스에 유저 정보 등록
        user = mAuth.getCurrentUser();
        uid= user.getUid();
        myRef.child("users").child(uid).child("name").setValue(user.getDisplayName());
        myRef.child("users").child(uid).child("email").setValue(user.getEmail());
        //데이터베이스에서 사업자확인 항목이 있는지 확인하기 위하여 불러옴
        myRef.child("users").child(uid).child("isBusiness(0(not),1(applying),2(finish))").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
//              Toast.makeText(LoginActivity.this, "로그인 사업자 여부 데이터 가져오기 성공", Toast.LENGTH_SHORT).show();
                // 사업자항목이 없으면 새로 생성
                if(dataSnapshot.getValue()==null) {
                    myRef.child("users").child(uid).child("isBusiness(0(not),1(applying),2(finish))").setValue(0);
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(LoginActivity.this, "로그인 사업자 여부 데이터 가져오기 실패", Toast.LENGTH_SHORT).show();
            }
        });

        //로그인이 되어있는 지 검사하기 위해 값을 읽는다
        myRef.child("users").child(uid).child("isLogin").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                //로그인이 되어있을 경우
                if(dataSnapshot.getValue()!=null&&dataSnapshot.getValue(Long.class) == 1) {
                    //푸쉬토큰 조회
                    myRef.child("users").child(uid).child("pushToken").addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            //이전 기기가 로그인이 되어 있다면
                            if(dataSnapshot.getValue()!=null&& !dataSnapshot.getValue(String.class).equals(FirebaseInstanceId.getInstance().getToken())) {
                                Toast.makeText(LoginActivity.this, "이전 기기의 로그인은 해제됩니다.", Toast.LENGTH_SHORT).show();

                                //이전에 로그인한 기기에 푸쉬알림을 보내 강제 로그아웃시킨다
                                RequestQueue queue = Volley.newRequestQueue(getApplicationContext());
                                MainActivity.send("", user.getEmail(), "1", dataSnapshot.getValue(String.class), queue);
                            }
                                //그 후 데이터베이스에 로그인 상태와 현재 로그인한 기기의 토큰을 등록
                                myRef.child("users").child(uid).child("isLogin").setValue(1);
                                myRef.child("users").child(uid).child("pushToken").setValue(FirebaseInstanceId.getInstance().getToken(), new DatabaseReference.CompletionListener() {
                                    //토큰 등록이 완료되면 창을 닫는다
                                    @Override
                                    public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                                        if(databaseError==null) {
                                            //경고창을 닫고
                                            dialog.dismiss();
                                            //홈화면으로 돌아가는 작업을 한다.
                                            intent = new Intent();
                                            //인텐트로 OK값을 보내고 홈 화면에서는 온액티비티리슐트로 받는다.
                                            setResult(RESULT_OK, intent);
                                            //창 닫기
                                            finish();
                                        }
                                        else{
                                            dialog.dismiss();
                                            Toast.makeText(LoginActivity.this, "푸쉬토큰 데이터베이스 입력 오류 : "+databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                });

                        }
                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                            Toast.makeText(LoginActivity.this, "푸쉬토큰 데이터 조회 캔슬됨 에러 메세지 : "+databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                            dialog.dismiss();
                        }
                    });
                }

                //로그인이 안 되어있거나 처음 계정을 등록하는 경우
                else {
                    myRef.child("users").child(uid).child("isLogin").setValue(1);
                    myRef.child("users").child(uid).child("pushToken").setValue(FirebaseInstanceId.getInstance().getToken(), new DatabaseReference.CompletionListener() {
                        //토큰 등록이 완료되면 창을 닫는다
                        @Override
                        public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                            //경고창을 닫고
                            dialog.dismiss();
                            //홈화면으로 돌아가는 작업을 한다.
                            intent = new Intent();
                            //인텐트로 OK값을 보내고 홈 화면에서는 온액티비티리슐트로 받는다.
                            setResult(RESULT_OK, intent);
                            //창 닫기
                            finish();
                        }
                    });
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(LoginActivity.this, "로그인 여부 데이터 조회 캔슬됨 에러 메세지 : "+databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                dialog.dismiss();
            }
        });

    }

    //구글 로그인 관련 리스너
    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Toast.makeText(this, "구글 연결 실패", Toast.LENGTH_SHORT).show();
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
