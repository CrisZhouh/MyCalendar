package com.example.mycalendar;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Calendar;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{

    private CalendarView calendarView;
    private EditText scheduleInput;
    private Context context;
    private Button addSchedule,checkAdd;
    private String dateToday;//用于记录今天的日期
    private MySQLiteOpenHelper mySQLiteOpenHelper;
    private SQLiteDatabase myDatabase;
    private TextView mySchedule[] = new TextView[5];
    private final String TAG = "myTag";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initView();

        //这里不这样的话一进去就设置当天的日程会报错
        Calendar time = Calendar.getInstance();
        int year = time.get(Calendar.YEAR);
        int month = time.get(Calendar.MONTH)+1;//注意要+1，0表示1月份
        int day = time.get(Calendar.DAY_OF_MONTH);
        dateToday = year+"-"+month+"-"+day;
        //还要直接查询当天的日程，这个要放在initView的后面，不然会出问题
        queryByDate(dateToday);
    }

    private void initView() {
        mySQLiteOpenHelper = new MySQLiteOpenHelper(this);
        myDatabase = mySQLiteOpenHelper.getWritableDatabase();

        context = this;
        addSchedule = findViewById(R.id.addSchedule);
        addSchedule.setOnClickListener(this);
        checkAdd = findViewById(R.id.checkAdd);
        checkAdd.setOnClickListener(this);

        calendarView = findViewById(R.id.calendar);
        scheduleInput = findViewById(R.id.scheduleDetailInput);

        calendarView.setOnDateChangeListener(mySelectDate);

        mySchedule[0] = findViewById(R.id.schedule1);
        mySchedule[1] = findViewById(R.id.schedule2);
        mySchedule[2] = findViewById(R.id.schedule3);
        mySchedule[3] = findViewById(R.id.schedule4);
        mySchedule[4] = findViewById(R.id.schedule5);
        for(TextView v:mySchedule){
            v.setOnClickListener(this);
        }
    }

    private CalendarView.OnDateChangeListener mySelectDate = new CalendarView.OnDateChangeListener() {
        @Override
        public void onSelectedDayChange(CalendarView view, int year, int month, int dayOfMonth) {
            dateToday = year+"-"+(month+1)+"-"+dayOfMonth;
            Toast.makeText(context, "你选择了:"+dateToday, Toast.LENGTH_SHORT).show();

            //得把用别的日期查出来的日程删除并将其隐藏
            for(TextView v:mySchedule){
                v.setText("");
                v.setVisibility(View.GONE);
            }
            queryByDate(dateToday);
        }
    };

    //根据日期查询日程
    private void queryByDate(String date) {
        //columns为null 查询所有列
        Cursor cursor = myDatabase.query("schedules",null,"time=?",new String[]{date},null,null,null);
        if(cursor.moveToFirst()){
            int scheduleCount = 0;
            do{
                @SuppressLint("Range") String aScheduleDetail = cursor.getString(cursor.getColumnIndex("scheduleDetail"));
                mySchedule[scheduleCount].setText("日程"+(scheduleCount+1)+"："+aScheduleDetail);
                mySchedule[scheduleCount].setVisibility(View.VISIBLE);
                scheduleCount++;
                //一定要有这句 不然TextView不够多要数组溢出了
                if(scheduleCount >= 5)
                    break;
            }while (cursor.moveToNext());
        }
        cursor.close();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.addSchedule:
                addMySchedule();
                break;
            case R.id.checkAdd:
                checkAddSchedule();
                break;
            case R.id.schedule1:case R.id.schedule2:case R.id.schedule3:case R.id.schedule4:case R.id.schedule5:
                editSchedule(v);
                break;
        }
    }

    private void editSchedule(View v) {
        Intent intent = new Intent(MainActivity.this, EditScheduleActivity.class);
        String sch = ((TextView) v).getText().toString().split("：")[1];
        intent.putExtra("schedule",sch);
        try{

        }catch(Exception e){
            Log.i(TAG, "editSchedule: "+e.getMessage());
        }
        startActivity(intent);
    }

    private void checkAddSchedule() {
        ContentValues values = new ContentValues();
        //第一个参数是表中的列名
        values.put("scheduleDetail",scheduleInput.getText().toString());
        values.put("time",dateToday);
        myDatabase.insert("schedules",null,values);
        scheduleInput.setVisibility(View.GONE);
        checkAdd.setVisibility(View.GONE);
        queryByDate(dateToday);
        //添加完以后把scheduleInput中的内容清除
        scheduleInput.setText("");
    }

    private void addMySchedule() {
        scheduleInput.setVisibility(View.VISIBLE);
        checkAdd.setVisibility(View.VISIBLE);
    }
}
