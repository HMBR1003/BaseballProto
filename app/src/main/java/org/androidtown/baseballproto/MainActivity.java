package org.androidtown.baseballproto;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.databinding.DataBindingUtil;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.FacebookSdk;
import com.facebook.login.LoginManager;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.androidtown.baseballproto.databinding.ActivityMainBinding;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;

import butterknife.BindView;
import devlight.io.library.ntb.NavigationTabBar;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {
    public static final int LOGIN_REQUEST = 100;
    public static final int BUSINESS_SIGNUP_REQUEST = 200;
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


        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);


        //왼쪽 슬라이드 메뉴 로그인과 로그아웃버튼 아이디 할당
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

        mAuth = FirebaseAuth.getInstance();
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    //데이터베이스 유저 영역 참조변수 선언 및 초기화
                    DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users");

                    //데이터베이스에서 유저가 고객인지 사업자 등록중인지 사업자인지 담는 정보를 불러옴
                    userRef.child(user.getUid()).child("isBusiness(0(not),1(applying),2(finish))").addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            isBusiness = dataSnapshot.getValue(Integer.class);
                            if(isBusiness==0){  //고객
                                userName.setText(user.getDisplayName()+"고객님");
                            }
                            else if(isBusiness==1){ //사업자 등록 신청한 사람
                                userName.setText(user.getDisplayName()+"고객님\n사업자 등록 신청중입니다.");
                                navNewBusiness.setTitle("사업자 신청정보 수정");
                            }
                            else if(isBusiness==2){ //사업자
                                userName.setText(user.getDisplayName()+"점주님\n");
                                navCart.setVisible(false);
                                navOrderList.setTitle("주문 받은 내역");
                                navReviewManage.setTitle("메뉴 관리");
                                navChangeCol.setVisible(false);
                                navNewBusiness.setTitle("매장 정보 수정");
                            }
                            else
                                Toast.makeText(MainActivity.this, "사업자여부 데이터가 0,1,2중 하나가 아닙니다.", Toast.LENGTH_SHORT).show();
//                    Toast.makeText(MainActivity.this, "메인 사업자여부 데이터 가져오기 성공", Toast.LENGTH_SHORT).show();
                        }
                        @Override
                        public void onCancelled(DatabaseError databaseError) {
//                            Toast.makeText(MainActivity.this, "메인 사업자여부 데이터 가져오기 실패", Toast.LENGTH_SHORT).show();
                        }
                    });
                    //내비게이션 메뉴 설정
                    navLogin.setTitle("로그아웃");
                    userEmail.setText(user.getEmail());
                } else {
                    userName.setText("로그인이 필요합니다");
                    userEmail.setText("");
                    navLogin.setTitle("로그인");
                    navCart.setVisible(true);
                    navOrderList.setTitle("주문 내역");
                    navReviewManage.setTitle("리뷰 관리");
                    navChangeCol.setVisible(true);
                    navNewBusiness.setTitle("사업자 신규등록 신청");
                }
            }
        };
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        // Inflate the menu; this adds items to the action bar if it is present.
//        getMenuInflater().inflate(R.menu.main, menu);
//        return true;
//    }

    //    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//        // Handle action bar item clicks here. The action bar will
//        // automatically handle clicks on the Home/Up button, so long
//        // as you specify a parent activity in AndroidManifest.xml.
//        int id = item.getItemId();
//
//        //noinspection SimplifiableIfStatement
//        if (id == R.id.action_settings) {
//            return true;
//        }
//
//        return super.onOptionsItemSelected(item);
//    }
    //좌측 네비게이션 바
    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        user = FirebaseAuth.getInstance().getCurrentUser();
        if (id == R.id.nav_login) {

            if(user==null) {
                Intent intent = new Intent(this, LoginActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivityForResult(intent, LOGIN_REQUEST);
            }
            else {
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setTitle("로그아웃 확인");
                builder.setMessage("로그아웃 하시겠습니까?");
                builder.setPositiveButton("예", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mAuth.signOut();
                        LoginManager.getInstance().logOut();
                        Toast.makeText(MainActivity.this, "로그아웃 되었습니다.", Toast.LENGTH_SHORT).show();
                    }
                });
                builder.setNegativeButton("아니오", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });
                AlertDialog dialog = builder.create();
                dialog.setCancelable(false);
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

            }
        } else if (id == R.id.nav_changeCol) {  //왼쪽 슬라이드메뉴 경기장 변경 부분

        } else if (id == R.id.nav_newBusiness) {  //왼쪽 슬라이드메뉴 사업자 신규 등록 부분
            if(user==null) {
                pleaseLogin();
            }
            else {
                Intent intent = new Intent(this, BusinessSignupActivity.class);
                intent.putExtra("uid",uid);
                intent.putExtra("isBusiness",isBusiness);
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
    public void onResume() { //온스타트 시 파이어베이스 계정 객체에 리스너 부착
        super.onResume();
        user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            uid=user.getUid();
        }
        mAuth.addAuthStateListener(mAuthListener);
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
                        .title("야구 정보")
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
}

