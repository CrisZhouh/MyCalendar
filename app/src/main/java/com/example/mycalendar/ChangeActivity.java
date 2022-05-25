package com.example.mycalendar;

import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class ChangeActivity extends AppCompatActivity {

    private MySQLiteOpenHelper mySQLiteOpenHelper;
    private SQLiteDatabase myDatabase;
    private EditText et1;
    private EditText et2;
    private Button bt;
    private String day;
    private String thing;
    private int id;
    private String change_detail;
    private String change_time;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change);

        mySQLiteOpenHelper = new MySQLiteOpenHelper(this);
        myDatabase = mySQLiteOpenHelper.getWritableDatabase();
        et1 = (EditText)findViewById(R.id.change_ed1);
        et2 = (EditText)findViewById(R.id.change_ed2);
        bt = (Button)findViewById(R.id.change_bt);

        Intent intent = getIntent();
        day = intent.getStringExtra("day");
        thing = intent.getStringExtra("thing");
        id = Integer.parseInt(intent.getStringExtra("id"));

        set();

        change();
    }

    public void set(){
        et1.setText(day);
        et2.setText(thing);
    }

    public void change(){
        bt.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                change_time = et1.getText().toString();
                change_detail = et2.getText().toString();
                myDatabase.execSQL("update schedules set scheduleDetail='"+change_detail+"' where id="+id);
                myDatabase.execSQL("update schedules set time='"+change_time+"' where id="+id);
                Toast.makeText(ChangeActivity.this,"修改成功！！！",Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(ChangeActivity.this,SearchActivity.class);
                startActivity(intent);
            }
        });
    }
}
