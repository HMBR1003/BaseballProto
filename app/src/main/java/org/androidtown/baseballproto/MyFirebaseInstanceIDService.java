package org.androidtown.baseballproto;

import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;

public class MyFirebaseInstanceIDService extends FirebaseInstanceIdService {
    private static final String TAG = "MyIID";

    @Override
    public void onTokenRefresh() {
        Log.d(TAG, "onTokenRefresh() 호출됨.");
        //리프레쉬된 토큰 변수에 저장
        String refreshedToken = FirebaseInstanceId.getInstance().getToken();
        Log.d(TAG, "Refreshed Token : " + refreshedToken);

        //로그인된 유저가 있다면 유저 데이터베이스에 토큰 저장
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if(user!=null) {
            DatabaseReference myRef = FirebaseDatabase.getInstance().getReference();
            myRef.child("users").child(user.getUid()).child("pushToken").setValue(FirebaseInstanceId.getInstance().getToken());
        }

    }

}
