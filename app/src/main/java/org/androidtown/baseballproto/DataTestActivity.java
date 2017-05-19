package org.androidtown.baseballproto;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;

import butterknife.BindView;

/**
 * Created by Administrator on 2017-05-16.
 */

public class DataTestActivity extends AppCompatActivity {
    ListView listView;
    DataTestAdapter adapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_datatest);
        adapter=new DataTestAdapter();

        adapter.addItem(new TestData("1번이얌","010-1111-1111",1,R.drawable.singer));
        adapter.addItem(new TestData("2번이얌","010-2111-1111",2,R.drawable.singer2));
        adapter.addItem(new TestData("3번이얌","010-3111-1111",3,R.drawable.singer3));
        adapter.addItem(new TestData("4번이얌","010-4111-1111",4,R.drawable.singer4));
        adapter.addItem(new TestData("5번이얌","010-5111-1111",5,R.drawable.singer5));
        adapter.addItem(new TestData("5번이얌","010-5111-1111",5,R.drawable.singer5));
        adapter.addItem(new TestData("5번이얌","010-5111-1111",5,R.drawable.singer5));
        adapter.addItem(new TestData("5번이얌","010-5111-1111",5,R.drawable.singer5));
        adapter.addItem(new TestData("5번이얌","010-5111-1111",5,R.drawable.singer5));
        adapter.addItem(new TestData("5번이얌","010-5111-1111",5,R.drawable.singer5));
        adapter.addItem(new TestData("5번이얌","010-5111-1111",5,R.drawable.singer5));
        adapter.addItem(new TestData("5번이얌","010-5111-1111",5,R.drawable.singer5));



        listView = (ListView)findViewById(R.id.listView);
        listView.setAdapter(adapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                TestData item = (TestData)adapter.getItem(position);
                Toast.makeText(DataTestActivity.this,item.getName()+ "을 선택함 ", Toast.LENGTH_SHORT).show();
            }
        });
    }

    class DataTestAdapter extends BaseAdapter {
        ArrayList<TestData> items = new ArrayList<TestData>();

        @Override
        public int getCount() {
            return items.size();
        }

        public void addItem(TestData item){
            items.add(item);
        }

        @Override
        public Object getItem(int position) {
            return items.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            DataTestView view = new DataTestView(getApplicationContext());
            TestData item = items.get(position);
            view.setName(item.getName());
            view.setMobile(item.getMobile());
            view.setAge(item.getAge());
            view.setImage(item.getResId());
            return view;
        }
    }
}

