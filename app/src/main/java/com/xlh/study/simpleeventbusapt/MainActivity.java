package com.xlh.study.simpleeventbusapt;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.xlh.study.eventbus.annotation.WxSubscribe;
import com.xlh.study.eventbus.annotation.mode.ThreadMode;
import com.xlh.study.eventbus.apt.EventBusIndex;
import com.xlh.study.eventbus.library.EventBus;
import com.xlh.study.simpleeventbusapt.bean.MessageBean;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    Button btnjump, btnSend;
    TextView tvContent;

    private static final int SUCCESS = 0;


    private Handler mHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            if (msg.what == SUCCESS) {
                MessageBean mb = (MessageBean) msg.obj;
                tvContent.setText(String.format("接收到的消息--priority：\n %s", mb.toString()));
            }
            return true;
        }
    });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initView();
        initListener();

        // EventBusIndex：编译时生成该类，在app\build\generated\ap_generated_sources\debug\out\com\xlh\study\eventbus\apt\EventBusIndex.java
        EventBus.getDefault().addIndex(new EventBusIndex());
        EventBus.getDefault().register(this);

    }

    private void initView() {
        btnjump = findViewById(R.id.btn_jump);
        btnSend = findViewById(R.id.btn_send);
        tvContent = findViewById(R.id.tv_content);
    }

    private void initListener() {
        btnjump.setOnClickListener(this);
        btnSend.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_jump:
                startSecondActivity();
                break;
            case R.id.btn_send:
                sendMessage();
                break;
            default:
                break;
        }
    }


    private void startSecondActivity() {
        Intent intent = new Intent();
        intent.setClass(this, SecondActivity.class);
        startActivity(intent);
    }

    private void sendMessage() {

        MessageBean bean = new MessageBean();
        bean.setTitle("MainActivity标题");
        bean.setContent("MainActivity内容");

        EventBus.getDefault().postSticky(bean);
    }



    // priority默认0
    @WxSubscribe(threadMode = ThreadMode.ASYNC)
    public void receiveMessageAsync(MessageBean mb) {
        Message msg = Message.obtain();
        msg.obj = mb;
        msg.what = SUCCESS;
        mHandler.sendMessage(msg);
        Log.e(Constants.TAG, "receiveMessageAsync  " + mb.toString());
        Log.e(Constants.TAG, "MainActivity-->Thread Name:" + Thread.currentThread().getName());
    }

    // priority = 1时，比receiveMessageAsync优先执行
    @WxSubscribe(threadMode = ThreadMode.ASYNC, priority = 1)
    public void receiveMessageAsyncPriority(MessageBean mb) {

        Message msg = Message.obtain();
        msg.obj = mb;
        msg.what = SUCCESS;
        mHandler.sendMessage(msg);

        Log.e(Constants.TAG, "receiveMessageAsyncPriority  " + mb.toString());
        Log.e(Constants.TAG, "MainActivity-->Thread Name:" + Thread.currentThread().getName());
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this);
        }
        EventBus.clearCaches();

        if (mHandler != null) {
            mHandler.removeCallbacksAndMessages(null);
        }

    }

}
