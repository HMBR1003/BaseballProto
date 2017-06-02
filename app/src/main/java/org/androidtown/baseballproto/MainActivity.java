package org.androidtown.baseballproto;

import android.app.NotificationManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.facebook.FacebookSdk;
import com.facebook.login.LoginManager;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;

import org.androidtown.baseballproto.BusinessMan.BusinessSignupActivity;
import org.androidtown.baseballproto.BusinessMan.MenuManageActivity;
import org.androidtown.baseballproto.MainFragment.BaseInfoFragment;
import org.androidtown.baseballproto.MainFragment.DeliveryFragment;
import org.androidtown.baseballproto.MainFragment.HomeFragment;
import org.androidtown.baseballproto.MainFragment.TakeoutFragment;
import org.androidtown.baseballproto.databinding.ActivityMainBinding;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import devlight.io.library.ntb.NavigationTabBar;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {
    public static final int LOGIN_REQUEST = 100;
    public static final int BUSINESS_SIGNUP_REQUEST = 200;
    static int pushCount = 0;
    static int loginCount = 0;
    HomeFragment homeFragment;
    DeliveryFragment deliveryFragment;
    TakeoutFragment takeoutFragment;
    BaseInfoFragment baseInfoFragment;
    ViewPager viewPager;
    int isBusiness;


    ActivityMainBinding mainBinding;
    TextView userEmail;
    TextView userName;
    MenuItem navLogin;
    MenuItem navCart;
    MenuItem navOrderList;
    MenuItem navReviewManage;
    MenuItem navChangeCol;
    MenuItem navNewBusiness;

    FirebaseAuth mAuth;
    FirebaseAuth.AuthStateListener mAuthListener;
    FirebaseUser user;
    DatabaseReference myRef;
    String uid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FacebookSdk.sdkInitialize(getApplicationContext());
        mainBinding = DataBindingUtil.setContentView(this, R.layout.activity_main);

        homeFragment = new HomeFragment();
        deliveryFragment = new DeliveryFragment();
        takeoutFragment = new TakeoutFragment();
        baseInfoFragment = new BaseInfoFragment();

        initUI();  //하단 UI 세팅

        //상단 UI 세팅
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, mainBinding.drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        mainBinding.drawerLayout.setDrawerListener(toggle);
        toggle.syncState();

        //왼쪽 슬라이드 메뉴 할당 및 리스너 부착
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);


        //왼쪽 슬라이드 버튼들 아이디 할당
        Menu menu = navigationView.getMenu();
        navLogin=menu.findItem(R.id.nav_login);
        navNewBusiness=menu.findItem(R.id.nav_newBusiness);
        navReviewManage=menu.findItem(R.id.nav_reviewManage);
        navCart=menu.findItem(R.id.nav_cart);
        navOrderList=menu.findItem(R.id.nav_orderList);
        navChangeCol=menu.findItem(R.id.nav_changeCol);

        //왼쪽 슬라이드 메뉴 유저이메일과 유저 이름 아이디 할당
        View headerView = navigationView.getHeaderView(0);
        userEmail = (TextView) headerView.findViewById(R.id.userEmail);
        userName = (TextView) headerView.findViewById(R.id.userName);

        //파이어베이스 인증 객체 할당
        mAuth = FirebaseAuth.getInstance();
        //좌측 UI 세팅
        setLeftMenu();
        //인증 객체에 붙일 인증 상태 감시 리스너 정의
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                //인증 상태가 바뀔 때마다 좌측 UI세팅
                setLeftMenu();
            }
        };
    }
    public void setBadge(){
        Intent intent = new Intent("android.intent.action.BADGE_COUNT_UPDATE");
        intent.putExtra("badge_count", pushCount+loginCount);
        //앱의  패키지 명
        intent.putExtra("badge_count_package_name","org.androidtown.baseballproto");
        // AndroidManifest.xml에 정의된 메인 activity 명
        intent.putExtra("badge_count_class_name", "org.androidtown.baseballproto.MainActivity");
        sendBroadcast(intent);
    }

    //좌측 UI 세팅하는 함수
    public void setLeftMenu(){
        //현재 로그인한 유저 객체를 가져옴
        user = mAuth.getCurrentUser();
            //로그인한 유저가 있으면
            if (user != null) {
                //데이터베이스 유저 영역 참조변수 선언 및 초기화
                DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users");

                //데이터베이스에서 유저가 고객인지 사업자 등록중인지 사업자인지 담는 정보를 불러옴
                userRef.child(user.getUid()).child("isBusiness(0(not),1(applying),2(finish))").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        //데이터 읽기가 완료 된 후의 동작
                        try {
                            //공통적인 로그인과 이메일 출력창 세팅
                            navLogin.setVisible(true);
                            navLogin.setTitle("로그아웃");
                            userEmail.setText(user.getEmail());

                            //유저의 사업자여부 데이터를 가져옴
                            isBusiness = dataSnapshot.getValue(Integer.class);

                            //고객일 경우
                            if (isBusiness == 0) {
                                userName.setText(user.getDisplayName() + "고객님");
                                navOrderList.setTitle("주문 내역");
                                navReviewManage.setTitle("리뷰 관리");
                                navNewBusiness.setTitle("사업자 신규등록 신청");

                                //숨겼던 메뉴 보이게 함
                                navCart.setVisible(true);
                                navOrderList.setVisible(true);
                                navReviewManage.setVisible(true);
                                navNewBusiness.setVisible(true);
                                navChangeCol.setVisible(true);
                            }
                            //사업자 등록 신청한 사람일 경우
                            else if (isBusiness == 1) {
                                userName.setText(user.getDisplayName() + "고객님\n사업자 등록 신청중입니다.");
                                navOrderList.setTitle("주문 내역");
                                navReviewManage.setTitle("리뷰 관리");
                                navNewBusiness.setTitle("사업자 신청정보 수정");

                                //숨겼던 메뉴 보이게 함
                                navCart.setVisible(true);
                                navOrderList.setVisible(true);
                                navReviewManage.setVisible(true);
                                navNewBusiness.setVisible(true);
                                navChangeCol.setVisible(true);
                            }
                            //사업자 등록이 완료된 사람일 경우
                            else if (isBusiness == 2) {
                                userName.setText(user.getDisplayName() + "점주님\n");
                                navCart.setVisible(false);
                                navOrderList.setTitle("주문 받은 내역");
                                navReviewManage.setTitle("메뉴 관리");
                                navNewBusiness.setTitle("매장 정보 수정");

                                //숨겼던 메뉴 보이게 함
                                navOrderList.setVisible(true);
                                navReviewManage.setVisible(true);
                                navNewBusiness.setVisible(true);
                                navChangeCol.setVisible(true);
                            } else
                                Toast.makeText(MainActivity.this, "사업자여부 데이터가 0,1,2중 하나가 아닙니다.", Toast.LENGTH_SHORT).show();
//                    Toast.makeText(MainActivity.this, "메인 사업자여부 데이터 가져오기 성공", Toast.LENGTH_SHORT).show();
                       }catch(NullPointerException e){
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
//                            Toast.makeText(MainActivity.this, "메인 사업자여부 데이터 가져오기 실패", Toast.LENGTH_SHORT).show();
                    }
                });
            //로그인한 유저가 없을 경우 초기화면으로 설정
            } else {
                userName.setText("로그인이 필요합니다");
                userEmail.setText("");
                navLogin.setTitle("로그인");
                navOrderList.setTitle("주문 내역");
                navReviewManage.setTitle("리뷰 관리");
                navNewBusiness.setTitle("사업자 신규등록 신청");

                navLogin.setVisible(true);
                navCart.setVisible(true);
                navOrderList.setVisible(true);
                navReviewManage.setVisible(true);
                navChangeCol.setVisible(true);
                navNewBusiness.setVisible(true);
            }
    }

    //뒤로가기 버튼 눌렸을 때 동작설정 왼쪽 메뉴가 열렸을 때 뒤로가기 버튼 누르면 왼쪽 메뉴가 닫히게한다.
    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    //로그아웃 동작 하는 함수
    public static void singOut(){
        FirebaseAuth.getInstance().signOut();
        LoginManager.getInstance().logOut();
    }

    //좌측 네비게이션 바 리스너 동작 설정
    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        int id = item.getItemId();  //눌려진 버튼의 아이디를 저장할 변수
        //유저 객체를 가져옴
        user = mAuth.getCurrentUser();

        //로그인 버튼 동작
        if (id == R.id.nav_login) {
            //로그인한 유저가 없을 경우엔 로그인창을 띄우는 동작을 함함
            if(user==null) {
                Intent intent = new Intent(this, LoginActivity.class);
               intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivityForResult(intent, LOGIN_REQUEST);
            }
            //로그인한 유저가 있을 경우엔 로그아웃할 지 물어본 후 로그아웃함
            else {
                //로그아웃할 지 경고창을 띄워 물어본다
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setTitle("로그아웃 확인");
                builder.setMessage("로그아웃 하시겠습니까?");
                //예 버튼 추가하기
                builder.setPositiveButton("예", new DialogInterface.OnClickListener() {
                    //예 버튼 눌렀을 경우 로그아웃 시키기
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //db의 로그인 상태값을 0으로 만들어준 후
                        myRef.child("users").child(uid).child("isLogin").setValue(0);
                        //로그아웃 실행
                        MainActivity.singOut();
                        Toast.makeText(MainActivity.this, "로그아웃 되었습니다.", Toast.LENGTH_SHORT).show();
                    }
                });
                //아니오 버튼 추가
                builder.setNegativeButton("아니오", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });
                //정의한 경고창을 만든다
                AlertDialog dialog = builder.create();
                //다른화면을 눌러도 경고창이 꺼지지 않도록 설정
                dialog.setCancelable(false);
                //경고창을 띄운다
                dialog.show();
            }
        } else if (id == R.id.nav_cart) {  //왼쪽 슬라이드메뉴 장바구니 부분
            if(user==null) {
                pleaseLogin();
            }
            else {

            }
        } else if (id == R.id.nav_orderList) {  //왼쪽 슬라이드메뉴 주문내역 부분
            if(user==null) {
                pleaseLogin();
            }
            else {

            }
        } else if (id == R.id.nav_reviewManage) {  //왼쪽 슬라이드메뉴 리뷰관리 부분
            if(user==null) {
                pleaseLogin();
            }
            else {
                if(isBusiness==2){
                    Intent intent = new Intent(this, MenuManageActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivityForResult(intent, BUSINESS_SIGNUP_REQUEST);
                }
            }
        } else if (id == R.id.nav_changeCol) {  //왼쪽 슬라이드메뉴 경기장 변경 부분

        } else if (id == R.id.nav_newBusiness) {  //왼쪽 슬라이드메뉴 사업자 신규 등록 부분
            if(user==null) {
                pleaseLogin();
            }
            //사업자 등록신청하는 화면을 새로 띄운다
            else {
                Intent intent = new Intent(this, BusinessSignupActivity.class);
                //uid와 사업자여부와 사용자 정보를 인텐트에 넣어서 전달해준다.
                intent.putExtra("uid",uid);
                intent.putExtra("isBusiness",isBusiness);
                intent.putExtra("name",user.getDisplayName());
                intent.putExtra("email",user.getEmail());
                intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivityForResult(intent, BUSINESS_SIGNUP_REQUEST);
            }
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    //로그인 해달라는 창을 띄우는 메서드
    public void pleaseLogin() {
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle("알림");
        builder.setMessage("먼저 로그인을 해주세요");
        builder.setPositiveButton("로그인", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivityForResult(intent, LOGIN_REQUEST);
            }
        });
        builder.setNegativeButton("취소", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        AlertDialog dialog = builder.create();
        dialog.setCancelable(false);
        dialog.show();
    }
    //프래그먼트 어댑터
    private class FragmentAdapter extends FragmentStatePagerAdapter {

        public FragmentAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0:
                    return new HomeFragment();
                case 1:
                    return new DeliveryFragment();
                case 2:
                    return new TakeoutFragment();
                case 3:
                    return new BaseInfoFragment();
                default:
                    return null;
            }
        }

        @Override
        public int getCount() {
            return 4;
        }

    }

    @Override
    public void onResume() { //온리슘 시 파이어베이스 계정 객체에 리스너 부착
        super.onResume();
        pushCount=0;
        loginCount=0;
        setBadge();
        NotificationManager nm = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
        nm.cancelAll();
        //유저 객체를 가져옴
        user = mAuth.getCurrentUser();

        //유저가 로그인한 상태인 지 검사
        if (user != null) {
            uid=user.getUid();
            myRef = FirebaseDatabase.getInstance().getReference();

            //푸쉬토큰을 검사하여 다른 기기에서 로그인을 했는 지 확인한다
            myRef.child("users").child(uid).child("pushToken").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    //현재 기기가 로그인 된 것이 아니라면
                    if(dataSnapshot.getValue()!=null&& !dataSnapshot.getValue(String.class).equals(FirebaseInstanceId.getInstance().getToken())) {
                        //로그아웃 후에
                        MainActivity.singOut();
                        //인증 상태 리스너를 추가
                        mAuth.addAuthStateListener(mAuthListener);
                        setLeftMenu();
                        Toast.makeText(MainActivity.this, "다른 기기에서 로그인하여 로그아웃 되었습니다.", Toast.LENGTH_SHORT).show();
                    }
                    else{
                        mAuth.addAuthStateListener(mAuthListener);
                        setLeftMenu();
                    }
                }
                @Override
                public void onCancelled(DatabaseError databaseError) {
                    setLeftMenu();
                }
            });
        }
        else{
            mAuth.addAuthStateListener(mAuthListener);
            setLeftMenu();
        }
    }

    @Override
    public void onStop() {  //온스탑 시 파이어베이스 계정 객체에 리스너 제거
        super.onStop();
        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //로그인 액티비티에서 로그인 성공 응답을 보내왔을 경우
        if(requestCode==LOGIN_REQUEST && resultCode==RESULT_OK){

        }
        //사업자 신청 액티비티에서 성공 응답을 보내왔을 경우
        else if(requestCode==BUSINESS_SIGNUP_REQUEST && resultCode==RESULT_OK){
            //제출 확인 안내 메세지 띄우기
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("안내");
            builder.setMessage("신청 내용이 제출되었습니다.\n운영진이 확인 후 승인해드리겠습니다.\n신청내용은 언제든지 다시 수정하실 수 있습니다.");
            //확인 버튼설정 및 버튼을 눌렀을 때 동작 설정
            builder.setPositiveButton("확인", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                }
            });
            AlertDialog dialog = builder.create();
            dialog.setCancelable(false);
            dialog.show();
        }
    }

    //푸쉬 메세지를 수신하여 그 데이터를 인텐트로 받아온 경우의 동작
    @Override
    protected void onNewIntent(Intent intent) {
        if (intent != null) {
            processIntent(intent);
        }
        super.onNewIntent(intent);
    }
    private void processIntent(Intent intent) {
        String from = intent.getStringExtra("from");
        if (from == null) {
            return;
        }
        String title = intent.getStringExtra("title");
        String contents = intent.getStringExtra("contents");
    }



    //하단 네비게이션 바 활성화 함수
    private void initUI() {
        viewPager = (ViewPager) findViewById(R.id.vp_horizontal_ntb);
        viewPager.setAdapter(new FragmentAdapter(getSupportFragmentManager()));
//        viewPager.setAdapter(new PagerAdapter() {
//            @Override
//            public int getCount() {
//                return 4;
//            }
//
//            @Override
//            public boolean isViewFromObject(final View view, final Object object) {
//                return view.equals(object);
//            }
//
//            @Override
//            public void destroyItem(final View container, final int position, final Object object) {
//            }
//
//            @Override   //이부분에 프래그먼트로 변경 필요
//            public Object instantiateItem(final ViewGroup container, final int position) {
//                final View view;
//                switch (position) {
//                    case 0:
//                        view = LayoutInflater.from(getBaseContext()).inflate(R.layout.fragment_home, null, false);
//                        container.addView(view);
//                        return view;
//                    case 1:
//                        view = LayoutInflater.from(getBaseContext()).inflate(R.layout.fragment_delivery, null, false);
//                        container.addView(view);
//                        return view;
//                    case 2:
//                        view = LayoutInflater.from(getBaseContext()).inflate(R.layout.fragment_takeout, null, false);
//                        container.addView(view);
//                        return view;
//                    case 3:
//                        view = LayoutInflater.from(getBaseContext()).inflate(R.layout.fragment_baseinfo, null, false);
//                        container.addView(view);
//                        return view;
//                    default:
//                        return null;
//                }
//                final View view = LayoutInflater.from(
//                        getBaseContext()).inflate(R.layout.item_vp, null, false);
//
//                container.addView(view);
//                return view;
//            }
//        });

        final String[] colors = getResources().getStringArray(R.array.default_preview);

        final NavigationTabBar navigationTabBar = (NavigationTabBar) findViewById(R.id.ntb_horizontal);
        final ArrayList<NavigationTabBar.Model> models = new ArrayList<>();
        models.add(
                new NavigationTabBar.Model.Builder(
                        getResources().getDrawable(R.drawable.ic_first),
                        Color.parseColor(colors[0]))
//                        .selectedIcon(getResources().getDrawable(R.drawable.ic_sixth))
                        .title("홈")
                        .build()
        );
        models.add(
                new NavigationTabBar.Model.Builder(
                        getResources().getDrawable(R.drawable.ic_second),
                        Color.parseColor(colors[1]))
//                        .selectedIcon(getResources().getDrawable(R.drawable.ic_eighth))
                        .title("배달음식")
                        .build()
        );
        models.add(
                new NavigationTabBar.Model.Builder(
                        getResources().getDrawable(R.drawable.ic_third),
                        Color.parseColor(colors[2]))
//                        .selectedIcon(getResources().getDrawable(R.drawable.ic_seventh))
                        .title("테이크아웃")
                        .build()
        );
        models.add(
                new NavigationTabBar.Model.Builder(
                        getResources().getDrawable(R.drawable.ic_fourth),
                        Color.parseColor(colors[3]))
//                        .selectedIcon(getResources().getDrawable(R.drawable.ic_eighth))
                        .title("야구장 날씨")
                        .build()
        );

        navigationTabBar.setModels(models);
        navigationTabBar.setViewPager(viewPager, 0);
        navigationTabBar.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(final int position, final float positionOffset, final int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(final int position) {
//                navigationTabBar.getModels().get(position).hideBadge();
            }

            @Override
            public void onPageScrollStateChanged(final int state) {

            }
        });

        navigationTabBar.postDelayed(new Runnable() {
            @Override
            public void run() {
                for (int i = 0; i < navigationTabBar.getModels().size(); i++) {
                    final NavigationTabBar.Model model = navigationTabBar.getModels().get(i);
                    navigationTabBar.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            model.showBadge();
                        }
                    }, i * 100);
                }
            }
        }, 500);
    }

    //푸쉬 메세지 발송 기능 함수
    public static void send(String title,String content,String type,String regId, RequestQueue queue) {

        JSONObject requestData = new JSONObject();

        try {
            requestData.put("priority", "high");

            JSONObject dataObj = new JSONObject();
            dataObj.put("title", title);
            dataObj.put("content", content);
            dataObj.put("type", type);
            requestData.put("data", dataObj);

            JSONArray idArray = new JSONArray();
            idArray.put(0, regId);
            requestData.put("registration_ids", idArray);

        } catch(Exception e) {
            e.printStackTrace();
        }

        sendData(requestData, new SendResponseListener() {
            @Override
            public void onRequestCompleted() {
            }

            @Override
            public void onRequestStarted() {
            }

            @Override
            public void onRequestWithError(VolleyError error) {
            }
        },queue);

    }

    public interface SendResponseListener {
        public void onRequestStarted();
        public void onRequestCompleted();
        public void onRequestWithError(VolleyError error);
    }

    public static void sendData(JSONObject requestData, final SendResponseListener listener, RequestQueue queue) {
        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.POST,
                "https://fcm.googleapis.com/fcm/send",
                requestData,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        listener.onRequestCompleted();
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                listener.onRequestWithError(error);
            }
        }
        ) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String,String> params = new HashMap<String,String>();

                return params;
            }

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String,String> headers = new HashMap<String,String>();
                headers.put("Authorization","key=AAAAzoC_z-4:APA91bGOjx1glemYKqDZ5jjHkC405GlfJfupTo5U7O32y54C4AFmbPSJYybFEV0dcDLF4eUtygfgPPtHpP_VqxiTICxtSmQWDhSbo8C6LJ_VS5XGGTqq-jm-ig0PoC8p2XFM883ulPdB");

                return headers;
            }

            @Override
            public String getBodyContentType() {
                return "application/json";
            }
        };

        request.setShouldCache(false);
        listener.onRequestStarted();
        queue.add(request);
    }
}

