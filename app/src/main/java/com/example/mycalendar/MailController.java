package com.example.mycalendar;


import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.database.Cursor;
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
    EditText txtphoneNo;
    EditText txtMessage;
    TextView textView;
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

        sendBtn = (Button) findViewById(R.id.btnSendSMS);
        txtphoneNo = (EditText) findViewById(R.id.editTextPhoneNo);
        txtMessage = (EditText) findViewById(R.id.editTextSMS);
        textView = (TextView)findViewById(R.id.display);

        sendBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                textView.setText("");
                sendSMSMessage();
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
                    "SMS faild, please try again.",
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



}
