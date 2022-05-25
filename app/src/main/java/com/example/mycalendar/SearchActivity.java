package com.example.mycalendar;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;

public class SearchActivity extends AppCompatActivity {

    private MySQLiteOpenHelper mySQLiteOpenHelper;
    private SQLiteDatabase myDatabase;
    private EditText searchText;
    private Button search_btn;
    private Button back_btn;
    private ListView listView;
    private MyAdapter adapter = null;
    private List<String> list = new ArrayList<String>();
    private TextView tv;
    private CheckBox cb1;
    private CheckBox cb2;
    private CheckBox cb3;
    private CheckBox cb4;
    private CheckBox cb5;
    private Button myButton[] = new Button[5];
    private Button del;
    private int data[] = new int[5];
    private String thing[] = new String[5];
    private String day[] = new String[5];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        initView();//初始化组件
    }

    private void initView(){
        mySQLiteOpenHelper = new MySQLiteOpenHelper(this);
        myDatabase = mySQLiteOpenHelper.getWritableDatabase();//获取可修改的数据库

        searchText = (EditText)findViewById(R.id.search);//获取搜索框
        search_btn = (Button)findViewById(R.id.search_btn);//获取搜索按钮
        listView = (ListView)findViewById(R.id.listview);//获取搜索列表
        back_btn = (Button)findViewById(R.id.back_btn);//获取返回按钮
        tv = (TextView)findViewById(R.id.tv);//获取文本框
        cb1 = (CheckBox)findViewById(R.id.cb1);//获取复选框1
        cb2 = (CheckBox)findViewById(R.id.cb2);//获取复选框2
        cb3 = (CheckBox)findViewById(R.id.cb3);//获取复选框3
        cb4 = (CheckBox)findViewById(R.id.cb4);//获取复选框4
        cb5 = (CheckBox)findViewById(R.id.cb5);//获取复选框5
        myButton[0] = (Button)findViewById(R.id.bt1);//获取按钮1
        myButton[1] = (Button)findViewById(R.id.bt2);//获取按钮1
        myButton[2] = (Button)findViewById(R.id.bt3);//获取按钮1
        myButton[3] = (Button)findViewById(R.id.bt4);//获取按钮1
        myButton[4] = (Button)findViewById(R.id.bt5);//获取按钮1
        del = (Button)findViewById(R.id.del);//获取删除按钮

        setData();// 给listView设置adapter
        setListeners();// 设置监听
        search();
        run();
        delete();
        back();
    }

    public void delete(){
        del.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                if(cb1.isChecked()){
                    myDatabase.execSQL("delete from schedules where id="+data[0]);
                }
                if(cb2.isChecked()){
                    myDatabase.execSQL("delete from schedules where id="+data[1]);
                }
                if(cb3.isChecked()){
                    myDatabase.execSQL("delete from schedules where id="+data[2]);
                }
                if(cb4.isChecked()){
                    myDatabase.execSQL("delete from schedules where id="+data[3]);
                }
                if(cb5.isChecked()){
                    myDatabase.execSQL("delete from schedules where id="+data[4]);
                }
                Toast.makeText(SearchActivity.this, "删除成功！", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void run(){
        myButton[0].setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(SearchActivity.this,ChangeActivity.class);
                intent.putExtra("day",day[0]);
                intent.putExtra("thing",thing[0]);
                intent.putExtra("id",data[0]+"");
                startActivity(intent);
            }
        });
        myButton[1].setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(SearchActivity.this,ChangeActivity.class);
                intent.putExtra("day",day[1]);
                intent.putExtra("thing",thing[1]);
                intent.putExtra("id",data[1]);
                startActivity(intent);
            }
        });
        myButton[2].setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(SearchActivity.this,ChangeActivity.class);
                intent.putExtra("day",day[2]);
                intent.putExtra("thing",thing[2]);
                intent.putExtra("id",data[2]);
                startActivity(intent);
            }
        });
        myButton[3].setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(SearchActivity.this,ChangeActivity.class);
                intent.putExtra("day",day[3]);
                intent.putExtra("thing",thing[3]);
                intent.putExtra("id",data[3]);
                startActivity(intent);
            }
        });
        myButton[4].setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(SearchActivity.this,ChangeActivity.class);
                intent.putExtra("day",day[4]);
                intent.putExtra("thing",thing[4]);
                intent.putExtra("id",data[4]);
                startActivity(intent);
            }
        });
    }

    private void back(){
        back_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(SearchActivity.this,MainActivity.class);
                startActivity(intent);
            }
        });
    }

    @SuppressLint("Range")
    private void initData() {
        Cursor cursor = myDatabase.query("schedules",null,null,null,null,null,null);
        if (cursor.moveToFirst()){
            do{
                String name = cursor.getString(cursor.getColumnIndex("scheduleDetail"));
                list.add(name);
            }while (cursor.moveToNext());
        }
    }

    private void setData() {
        initData();// 初始化数据

        // 这里创建adapter的时候，构造方法参数传了一个接口对象，这很关键，回调接口中的方法来实现对过滤后的数据的获取
        adapter = new MyAdapter(list,this, new FilterListener() {
            // 回调方法获取过滤后的数据
            public void getFilterData(List<String> list) {
                // 这里可以拿到过滤后数据，所以在这里可以对搜索后的数据进行操作
                Log.e("TAG", "接口回调成功");
                Log.e("TAG", list.toString());
                setItemClick(list);
            }
        });
        listView.setAdapter(adapter);
    }


    protected void setItemClick(final List<String> filter_lists) {
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                // 点击对应的item时，将内容填充到编辑框中
                searchText.setText(filter_lists.get(position));
            }
        });
    }

    private void setListeners(){
        // 没有进行搜索的时候，也要添加对listView的item单击监听
        setItemClick(list);

        searchText.addTextChangedListener(new TextWatcher(){
            public void beforeTextChanged(CharSequence s,int start, int count, int after){
            }//文本改变之前执行
            @Override
            //文本改变的时候执行
            public void onTextChanged(CharSequence s, int start, int before,
                                      int count) {
                //将多余的控件隐藏
                cb1.setText("");
                cb2.setText("");
                cb3.setText("");
                cb4.setText("");
                cb5.setText("");
                tv.setVisibility(View.GONE);
                cb1.setVisibility(View.GONE);
                cb2.setVisibility(View.GONE);
                cb3.setVisibility(View.GONE);
                cb4.setVisibility(View.GONE);
                cb5.setVisibility(View.GONE);
                myButton[0].setVisibility(View.GONE);
                myButton[1].setVisibility(View.GONE);
                myButton[2].setVisibility(View.GONE);
                myButton[3].setVisibility(View.GONE);
                myButton[4].setVisibility(View.GONE);
                del.setVisibility(View.GONE);

                if(!searchText.getText().toString().equals("")){//已输入内容
                    listView.setVisibility(View.VISIBLE);//搜索列表显示
                }else{
                    listView.setVisibility(View.GONE);//搜索列表隐藏
                }
                // 如果adapter不为空的话就根据编辑框中的内容来过滤数据
                if(adapter != null){
                    adapter.getFilter().filter(s);
                }
            }
            public void afterTextChanged(Editable s) {
            }//文本改变之后执行
        });
    }

    @SuppressLint("Range")
    public void search(){
        search_btn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                int count = 0;
                listView.setVisibility(View.GONE);
                String text = searchText.getText().toString();
                boolean find = false;//判断是否能找到日程信息
                if(text.equals("")){
                    Toast.makeText(SearchActivity.this,"请输入要查询的内容再进行查询",Toast.LENGTH_SHORT).show();
                }else{
                    Cursor cursor = myDatabase.query("schedules",null,null,null,null,null,null);
                    if (cursor.moveToFirst()){
                        do{
                            String name = cursor.getString(cursor.getColumnIndex("scheduleDetail"));
                            String time = cursor.getString(cursor.getColumnIndex("time"));
                            int id = Integer.parseInt(cursor.getString(cursor.getColumnIndex("id")));
                            if(text.equals(name)){
                                data[count]=id;
                                thing[count]=name;
                                day[count]=time;
                                find = true;
//                                myButton[buttonCount].setText("日程"+(name+1)+"："+name);
//                                mySchedule[scheduleCount].setVisibility(View.VISIBLE);
                                count++;
                                //一定要有这句 不然TextView不够多要数组溢出了
                                if(count >= 5)
                                    break;
                                if(cb1.getText().toString().equals("")){
                                    cb1.setText("时间："+time+"     事件："+name);
                                    tv.setVisibility(View.VISIBLE);
                                    cb1.setVisibility(View.VISIBLE);
                                    myButton[0].setVisibility(View.VISIBLE);
                                    del.setVisibility(View.VISIBLE);
                                }
                                else if(cb2.getText().toString().equals("")){
                                    cb2.setText("时间："+time+"     事件："+name);
                                    cb2.setVisibility(View.VISIBLE);
                                    myButton[1].setVisibility(View.VISIBLE);
                                }
                                else if(cb3.getText().toString().equals("")){
                                    cb3.setText("时间："+time+"     事件："+name);
                                    cb3.setVisibility(View.VISIBLE);
                                    myButton[2].setVisibility(View.VISIBLE);
                                }
                                else if(cb4.getText().toString().equals("")){
                                    cb4.setText("时间："+time+"     事件："+name);
                                    cb4.setVisibility(View.VISIBLE);
                                    myButton[3].setVisibility(View.VISIBLE);
                                }
                                else if(cb5.getText().toString().equals("")){
                                    cb5.setText("时间："+time+"     事件："+name);
                                    cb5.setVisibility(View.VISIBLE);
                                    myButton[4].setVisibility(View.VISIBLE);
                                }
//                                AlertDialog.Builder builder = new AlertDialog.Builder(SearchActivity.this);
//                                builder.setMessage(
//                                        "         时       间：" + time + "\n" +
//                                                "         事       件：" + name);
//                                builder.setTitle("        查看信息");
//                                builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
//                                    @Override
//                                    public void onClick(DialogInterface dialogInterface, int i) {
//                                    }
//                                });
//                                builder.show();
                            }
                        }while (cursor.moveToNext());
                    }
                    if(find == false){
                        Toast.makeText(SearchActivity.this,"未找到该日程信息！！！",Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
    }
}
