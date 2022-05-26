package com.example.mycalendar;


import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.app.Activity;
import android.os.Handler;
import android.os.Message;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class MailController extends Activity {
    private final String TAG = "myTag";
    final private int REQUEST_CODE_ASK_PERMISSIONS = 123;
    Button sendBtn;
    Button sendBtn_add;
    Button sendBtn_delete;
    EditText txtphoneNo;
    EditText txtMessage;
    EditText txtMessage_add;
    EditText txtMessage_delete;
    TextView textView;
    private SQLiteDatabase myDatabase;
    private MySQLiteOpenHelper mySQLiteOpenHelper;
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
        setContentView(R.layout.mail_controller);
        mySQLiteOpenHelper = new MySQLiteOpenHelper(this);
        sendBtn = (Button) findViewById(R.id.btnSendSMS);
        sendBtn_add = (Button) findViewById(R.id.btnSendSMS_add);
        sendBtn_delete = (Button) findViewById(R.id.btnSendSMS_delete);

        txtphoneNo = (EditText) findViewById(R.id.editTextPhoneNo);
        txtMessage = (EditText) findViewById(R.id.editTextSMS);
        txtMessage_add = (EditText) findViewById(R.id.editTextSMS_add);
        txtMessage_delete = (EditText) findViewById(R.id.editTextSMS_delete);

        textView = (TextView)findViewById(R.id.display);

        sendBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                textView.setText("");
                sendSMSMessage();
            }
        });

        sendBtn_add.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                textView.setText("");
                sendSMSMessage_add();
            }
        });

        sendBtn_delete.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                textView.setText("");
                sendSMSMessage_delete();
            }
        });


        smsContentObserver = new SMSContentObserver(MailController.this, mHandler);
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


    protected void sendSMSMessage() {
        Log.i("Send SMS", "");

        String phoneNo = txtphoneNo.getText().toString();
        String message = txtMessage.getText().toString();

        try {
            SmsManager smsManager = SmsManager.getDefault();
            smsManager.sendTextMessage(phoneNo, null, message, null, null);
            Toast.makeText(getApplicationContext(), "SMS sent.",
                    Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            Toast.makeText(getApplicationContext(),
                    "SMS faild, please try again."+e.getMessage(),
                    Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }
    }
    protected void sendSMSMessage_add() {

        String phoneNo = txtphoneNo.getText().toString();
        String message = "add"+txtMessage_add.getText().toString()+"/"+txtMessage.getText().toString();
        Log.i(TAG, "sendSMSMessage_delete: "+message);
        try {
            onBackPressed(txtMessage.getText().toString(),txtMessage_add.getText().toString());
            SmsManager smsManager = SmsManager.getDefault();
            smsManager.sendTextMessage(phoneNo, null, message, null, null);
            Toast.makeText(getApplicationContext(), "SMS sent.",
                    Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            Toast.makeText(getApplicationContext(),
                    "SMS faild, please try again."+e.getMessage(),
                    Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }
    }
    protected void sendSMSMessage_delete() {

        String phoneNo = txtphoneNo.getText().toString();
        String message = "delete"+txtMessage_delete.getText().toString()+"/"+txtMessage.getText().toString();
        Log.i(TAG, "sendSMSMessage_delete: "+message);

        try {
            SmsManager smsManager = SmsManager.getDefault();
            smsManager.sendTextMessage(phoneNo, null, message, null, null);
            Toast.makeText(getApplicationContext(), "SMS sent.",
                    Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            Toast.makeText(getApplicationContext(),
                    "SMS faild, please try again."+e.getMessage(),
                    Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }
    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @SuppressLint("Range")
    private void setSmsCode() {
        String phoneNo = txtphoneNo.getText().toString();
        Boolean flag=false;
        Cursor cursor = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            int hasReadSmsPermission = checkSelfPermission(Manifest.permission.READ_SMS);
            if (hasReadSmsPermission != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.READ_SMS}, REQUEST_CODE_ASK_PERMISSIONS);
                return;
            }
        }
        try {//
            cursor = getContentResolver().query(
                    Uri.parse("content://sms/inbox"),
                    new String[] { "_id", "address", "read", "body", "date" },
                    "address = "+'\"'+phoneNo+'\"', null, "date desc"); // datephone想要的短信号码
            if (cursor != null) { // 当接受到的新短信与想要的短信做相应判断
                String body = "";
                while (cursor.moveToNext()&&!flag) {
                    body = cursor.getString(cursor.getColumnIndex("body"));// 在这里获取短信信息
                    textView.setText(body);
                    Log.i(TAG, "setSmsCode: "+body);
                    flag=true;

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


    public void onBackPressed(String date,String detail) {
        new AlertDialog.Builder(this).setTitle("是否也添加到你的日程中？")
                .setIcon(android.R.drawable.ic_dialog_info)
                .setPositiveButton("是", new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        myDatabase = mySQLiteOpenHelper.getWritableDatabase();
                        ContentValues values_add = new ContentValues();
                        //第一个参数是表中的列名
                        values_add.put("scheduleDetail",detail);
                        values_add.put("time",date);
                        myDatabase.insertWithOnConflict("schedules",null,values_add, SQLiteDatabase.CONFLICT_REPLACE);




//                        MailController.this.finish();

                    }
                })
                .setNegativeButton("否", new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // 点击“返回”后的操作,这里不设置没有任何操作
                    }
                }).show();
    }



}
