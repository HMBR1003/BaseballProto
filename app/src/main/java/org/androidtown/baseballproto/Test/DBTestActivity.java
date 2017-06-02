package org.androidtown.baseballproto.Test;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.androidtown.baseballproto.R;

import java.util.ArrayList;

/**
 * Created by Administrator on 2017-05-17.
 */

public class DBTestActivity extends AppCompatActivity {
    // DB에 저장시킬 데이터를 입력받는 EditText
    private EditText editText;

    // 입력받은 데이터를 저장시킬 버튼
    private Button inputBtn;

    // DB 데이터를 보여줄 ListView
    private ListView listView;

    private ArrayAdapter<String> dataAdapter;

    // DB 관련 변수
    private FirebaseDatabase database;
    private DatabaseReference myRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dbtest);

        // 변수 초기화
        editText = (EditText) findViewById(R.id.editText);
        inputBtn = (Button) findViewById(R.id.inputBtn);
        listView = (ListView) findViewById(R.id.listView);

        // DB 관련 변수 초기화
        database = FirebaseDatabase.getInstance();
        // message Reference가 없어도 상관 x
        myRef = database.getReference("message");

        // ListView에 출력할 데이터 초기화
        dataAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_dropdown_item_1line, new ArrayList<String>());

        // ListView에 Adapter 붙여줌
        listView.setAdapter(dataAdapter);

        // 자신이 얻은 Reference에 이벤트를 붙여줌
        // 데이터의 변화가 있을 때 출력해옴
        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // 데이터를 읽어올 때 모든 데이터를 읽어오기때문에 List 를 초기화해주는 작업이 필요하다.
                dataAdapter.clear();
                for (DataSnapshot messageData : dataSnapshot.getChildren()) {
                    String msg = messageData.getValue().toString();
                    dataAdapter.add(msg);
                }
                // notifyDataSetChanged를 안해주면 ListView 갱신이 안됨
                dataAdapter.notifyDataSetChanged();
                // ListView 의 위치를 마지막으로 보내주기 위함
                listView.setSelection(dataAdapter.getCount() - 1);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


        // 버튼 리스너 정의
        // 클릭시 EditText의 내용이 DB에 저장
        inputBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String str = editText.getText().toString().trim();
                // push는 firebase가 임의로 중복되지 않은 키를 생성해서 저장
                // push로 하지 않을 경우 덮어 씌움
                myRef.push().setValue(str);

                // EditText 초기화
                editText.setText("");
            }
        });

    }
}

//    @Override
//    protected void onCreate(@Nullable Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_dbtest);
//
//        FirebaseDatabase database = FirebaseDatabase.getInstance();
//        DatabaseReference myRef = database.getReference("message");
//
//
//        myRef.setValue("Hello, World!");
//
//    }
//}
