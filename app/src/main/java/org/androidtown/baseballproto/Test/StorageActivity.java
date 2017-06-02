package org.androidtown.baseballproto.Test;

import android.Manifest;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import org.androidtown.baseballproto.R;

public class StorageActivity extends AppCompatActivity {

    @Override
    protected void onResume() {
        super.onResume();
        int permissionCheck = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE);
        if(permissionCheck== PackageManager.PERMISSION_DENIED) {

            // 이 권한을 필요한 이유를 설명해야하는가?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_EXTERNAL_STORAGE)) {
                AlertDialog.Builder builder = new AlertDialog.Builder(StorageActivity.this);
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
                builder.setNegativeButton("닫기", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                });
                AlertDialog dialog = builder.create();
                dialog.setCancelable(false);
                dialog.show();
                // 다이어로그같은것을 띄워서 사용자에게 해당 권한이 필요한 이유에 대해 설명합니다
                // 해당 설명이 끝난뒤 requestPermissions()함수를 호출하여 권한허가를 요청해야 합니다

            } else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);

                // 필요한 권한과 요청 코드를 넣어서 권한허가요청에 대한 결과를 받아야 합니다

            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 1:
            case 2:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // 권한 허가
                    // 해당 권한을 사용해서 작업을 진행할 수 있습니다
                } else {
                    AlertDialog.Builder builder = new AlertDialog.Builder(StorageActivity.this);
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
                    builder.setNegativeButton("닫기", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            finish();
                        }
                    });
                    AlertDialog dialog = builder.create();
                    dialog.setCancelable(false);
                    dialog.show();
                    // 권한 거부
                    // 사용자가 해당권한을 거부했을때 해주어야 할 동작을 수행합니다
                }
                return;
        }
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_storage);


//        if (ContextCompat.checkSelfPermission(this,
//                Manifest.permission.READ_EXTERNAL_STORAGE)
//                != PackageManager.PERMISSION_GRANTED) {
//
//            // Should we show an explanation?
//            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
//                    android.Manifest.permission.READ_EXTERNAL_STORAGE)) {
//
//                // Show an expanation to the user *asynchronously* -- don't block
//                // this thread waiting for the user's response! After the user
//                // sees the explanation, try again to request the permission.
//
//            } else {
//
//                // No explanation needed, we can request the permission.
//
//                ActivityCompat.requestPermissions(this,
//                        new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE},
//                        1);
//
//                // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
//                // app-defined int constant. The callback method gets the
//                // result of the request.
//            }
//        }



        //스토리지 변수 생성
        FirebaseStorage storage = FirebaseStorage.getInstance();
        //스토리지에 대한 참조 생성. 스토리지 변수를 이용하여 생성해줌
        StorageReference storageRef = storage.getReferenceFromUrl("gs://fir-test-8ffe3.appspot.com/");
        //스토리지 참조의 images 폴더에 대한 참조 생성
        StorageReference imagesRef = storageRef.child("images");
        //스토리지 참조의 space.jpg 파일에 대한 참조 생성
        StorageReference spaceRef = storageRef.child("images/space.jpg");

        //space.jpg 파일의 바로 위 항목에 대한 참조
        imagesRef = spaceRef.getParent();

        //space.jpg 파일이 있는 위치의 가장 상위항목에 대한 참조
        StorageReference rootRef = spaceRef.getRoot();

        //child(), getParent() 및 getRoot()는 각각 참조를 반환하므로 여러 번 연결할 수 있습니다.
        StorageReference earthRef = spaceRef.getParent().child("earth.jpg");
        // 그러나 getRoot().getParent()를 호출하면 null이 반환됩니다.
        StorageReference nullRef = spaceRef.getRoot().getParent();

        //경로 반환
        spaceRef.getPath();

        //파일 이름 반환(형식까지)
        spaceRef.getName();

        //버킷 반환
        spaceRef.getBucket();
    }
}

