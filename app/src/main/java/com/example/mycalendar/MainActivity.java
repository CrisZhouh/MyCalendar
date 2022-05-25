package com.example.mycalendar;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import android.net.Uri;
import androidx.appcompat.app.AppCompatActivity;


import java.util.Calendar;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{

    private CalendarView calendarView;
    private EditText scheduleInput;
    private Context context;
    private Button addSchedule,checkAdd;
    private ImageButton mailButton;
    private String dateToday;//用于记录今天的日期
    private MySQLiteOpenHelper mySQLiteOpenHelper;
    private SQLiteDatabase myDatabase;
    private TextView mySchedule[] = new TextView[5];
    private final String TAG = "myTag";
    final private int REQUEST_CODE_ASK_PERMISSIONS = 123;
    private Button search_bt;

    private EditText login_et_sms_code;
    private SMSContentObserver smsContentObserver;
    protected static final int MSG_INBOX = 1;
    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_INBOX:
                    setSmsCode();
                    break;
            }
        }
    };




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


        smsContentObserver = new SMSContentObserver(MainActivity.this, mHandler);

    }

    private void initView() {
        mySQLiteOpenHelper = new MySQLiteOpenHelper(this);
        myDatabase = mySQLiteOpenHelper.getWritableDatabase();

        context = this;
        addSchedule = findViewById(R.id.addSchedule);
        addSchedule.setOnClickListener(this);
        checkAdd = findViewById(R.id.checkAdd);
        checkAdd.setOnClickListener(this);
        mailButton = findViewById(R.id.mailButton);
        mailButton.setOnClickListener(this);

        calendarView = findViewById(R.id.calendar);
        scheduleInput = findViewById(R.id.scheduleDetailInput);

        calendarView.setOnDateChangeListener(mySelectDate);

        mySchedule[0] = findViewById(R.id.schedule1);
        mySchedule[1] = findViewById(R.id.schedule2);
        mySchedule[2] = findViewById(R.id.schedule3);
        mySchedule[3] = findViewById(R.id.schedule4);
        mySchedule[4] = findViewById(R.id.schedule5);

        search_bt = (Button)findViewById(R.id.search_bt);

        for(TextView v:mySchedule){
            v.setOnClickListener(this);
        }

        Search();

    }

    private void Search(){
        search_bt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this,SearchActivity.class);
                startActivity(intent);
            }
        });
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
            case R.id.mailButton:
                try{
                    mail(v);
                }catch(Exception e){
                    Log.i(TAG, "onClick: "+e.getMessage());
                }
                break;
        }
    }

    @Override
    protected void onResume() {
        // TODO Auto-generated method stub
        super.onResume();
        if (smsContentObserver != null) {
            getContentResolver().registerContentObserver(
                    Uri.parse("content://sms/"), true, smsContentObserver);// 注册监听短信数据库的变化
        }
    }

    @Override
    protected void onPause() {
        // TODO Auto-generated method stub
        super.onPause();
        if (smsContentObserver != null) {
            getContentResolver().unregisterContentObserver(smsContentObserver);// 取消监听短信数据库的变化
        }

    }

    private void mail(View v) {
        Intent intent = new Intent(MainActivity.this, MailController.class);
//        String sch = ((TextView) v).getText().toString().split("：")[1];
//        intent.putExtra("schedule",sch);
        startActivity(intent);
    }

    private void editSchedule(View v) {
        Intent intent = new Intent(MainActivity.this, EditScheduleActivity.class);
        String sch = ((TextView) v).getText().toString().split("：")[1];
        intent.putExtra("schedule",sch);
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

    @SuppressLint("Range")
    private void setSmsCode() {
        Boolean flag=false;
        Cursor cursor = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            int hasReadSmsPermission = checkSelfPermission(Manifest.permission.READ_SMS);
            if (hasReadSmsPermission != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.READ_SMS}, REQUEST_CODE_ASK_PERMISSIONS);
                return;
            }
        }
        try {
            cursor = getContentResolver().query(
                    Uri.parse("content://sms/inbox"),
                    new String[] { "_id", "address", "read", "body", "date" },
                    null, null, "date desc"); // datephone想要的短信号码
            if (cursor != null) { // 当接受到的新短信与想要的短信做相应判断
                String body = "";
                String number = "";
                while (cursor.moveToNext()&&!flag) {
                    body = cursor.getString(cursor.getColumnIndex("body"));// 在这里获取短信信息
                    number = cursor.getString(cursor.getColumnIndex("address"));// 在这里获取短信信息
                    // 下面匹配验证码
                    Log.i(TAG, "setSmsCode: "+body);
                    String[]temp=MailqueryByDate(body);
                    for(int i =0;i<temp.length;i++){
                        Log.i(TAG, "setSmsCode: "+temp[i]);
                    }
                    sendMessage(number,temp);
                    flag=true;

//                    Pattern pattern = Pattern.compile("\\d{6}");
//                    Matcher matcher = pattern.matcher(body);
//                    if (matcher.find()) {
//                        String smsCodeStr = matcher.group(0);
//                        Log.i("fuyanan", "sms find: code=" + matcher.group(0));// 打印出匹配到的验证码
//                        login_et_sms_code.setText(smsCodeStr);
//                        break;
//                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    //2022-5-21
    private String[] MailqueryByDate(String date) {
        String[] dateEvent ;
        //columns为null 查询所有列
        Cursor cursor = myDatabase.query("schedules",null,"time=?",new String[]{date},null,null,null);
        int count=cursor.getCount();
        Log.i(TAG, "MailqueryByDate: "+count);
        dateEvent = new String[count];
        if(cursor.moveToFirst()){
            int scheduleCount=0;
            do{
                @SuppressLint("Range") String aScheduleDetail = cursor.getString(cursor.getColumnIndex("scheduleDetail"));
                dateEvent[scheduleCount]="日程"+(scheduleCount+1)+"："+aScheduleDetail;

                scheduleCount++;

            }while (cursor.moveToNext());
            return dateEvent;
        }
        cursor.close();
        return null;
    }


    private void sendMessage(String number,String[] message) {
        Log.i("Send SMS", "");
        String text="";
        for(int i=0;i<message.length;i++)text+=message[i]+'\n';

        try {
            SmsManager smsManager = SmsManager.getDefault();

            smsManager.sendTextMessage(number, null, text, null, null);
            Toast.makeText(getApplicationContext(), "SMS sent.",
                    Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            Toast.makeText(getApplicationContext(),
                    "SMS faild, please try again.",
                    Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }
    }
}
