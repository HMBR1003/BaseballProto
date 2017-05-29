package org.androidtown.baseballproto;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import com.facebook.login.LoginManager;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Map;

public class MyFirebaseMessagingService extends FirebaseMessagingService {
    private static final String TAG = "MyMS";
    NotificationManager notificationManager;

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        Log.d(TAG, "onMessageReceived() 호출됨.");

        //메세지 보낸 곳 발신자 코드 저장
        String from = remoteMessage.getFrom();

        //받아온 메세지내용 추출해서 저장
        Map<String, String> data = remoteMessage.getData();
        String title = data.get("title");
        String content = data.get("content");
        String type = data.get("type");

        Log.v(TAG, "from : " + from + ", content : " + content);

        notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        sendNotification(title,content,type);
        Intent intent = new Intent("android.intent.action.BADGE_COUNT_UPDATE");
        intent.putExtra("badge_count", MainActivity.pushCount+MainActivity.loginCount);
        //앱의  패키지 명
        intent.putExtra("badge_count_package_name","org.androidtown.baseballproto");
        // AndroidManifest.xml에 정의된 메인 activity 명
        intent.putExtra("badge_count_class_name", "org.androidtown.baseballproto.MainActivity");
        sendBroadcast(intent);
    }

    private void sendNotification(String title, String content, String type) {
        if(type.equals("1")){
            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            if(user!=null){
                MainActivity.singOut();
            }
            Intent intent = new Intent(this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            PendingIntent pendingIntent = PendingIntent.getActivity(this, 0 /* Request code */, intent,
                    PendingIntent.FLAG_ONE_SHOT);

            Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this)
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setContentTitle("로그인 알림")
                    .setContentText("다른 기기에서 로그인하였습니다.")
                    .setNumber(++MainActivity.loginCount)
//                    .setSubText(MainActivity.pushCount+"")
                    .setAutoCancel(true)
                    .setSound(defaultSoundUri)
                    .setDefaults(NotificationCompat.DEFAULT_VIBRATE)
                    .setContentIntent(pendingIntent)
                    .setPriority(Notification.PRIORITY_MAX)
                    .setGroupSummary(true)
                    .setGroup("abc");
            if(android.os.Build.VERSION.SDK_INT>=24){
                notificationBuilder.setSubText(MainActivity.loginCount+"");
            }

            notificationManager.notify(100 /* ID of notification */, notificationBuilder.build());
        }
        else {
            Intent intent = new Intent(this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            PendingIntent pendingIntent = PendingIntent.getActivity(this, 0 /* Request code */, intent,
                    PendingIntent.FLAG_ONE_SHOT);

            Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this)
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setContentTitle(title)
                    .setContentText(content)
                    .setNumber(++MainActivity.pushCount)
//                    .setSubText(MainActivity.pushCount+"")
                    .setAutoCancel(true)
                    .setSound(defaultSoundUri)
                    .setDefaults(NotificationCompat.DEFAULT_VIBRATE)
                    .setContentIntent(pendingIntent)
                    .setPriority(Notification.PRIORITY_MAX)
                    .setGroupSummary(true)
                    .setGroup("abc");
            if(android.os.Build.VERSION.SDK_INT>=24){
                notificationBuilder.setSubText(MainActivity.pushCount+"");
            }

            notificationManager.notify(200 /* ID of notification */, notificationBuilder.build());
        }
    }

}
