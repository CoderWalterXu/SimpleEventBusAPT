package com.xlh.study.simpleeventbusapt;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.xlh.study.eventbus.annotation.WxSubscribe;
import com.xlh.study.eventbus.annotation.mode.ThreadMode;
import com.xlh.study.eventbus.library.EventBus;
import com.xlh.study.simpleeventbusapt.bean.MessageBean;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

/**
 * @author: Watler Xu
 * time:2020/4/27
 * description:
 * version:0.0.1
 */
public class SecondActivity extends AppCompatActivity implements View.OnClickListener {

    Button btnSend, btnSendSticky;
    TextView tvContent;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_second);

        initView();

        initListener();

    }


    private void initView() {
        btnSend = findViewById(R.id.btn_send);
        btnSendSticky = findViewById(R.id.btn_send_sticky);
        tvContent = findViewById(R.id.tv_content);
    }

    private void initListener() {
        btnSend.setOnClickListener(this);
        btnSendSticky.setOnClickListener(this);
    }


    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_send:
                sendMessage();
                break;
            case R.id.btn_send_sticky:
                stickyMessage();
                break;
            default:
                break;
        }
    }

    private void sendMessage() {

        MessageBean bean = new MessageBean();
        bean.setTitle("SecondActivity标题");
        bean.setContent("SecondActivity内容");

        EventBus.getDefault().post(bean);

//        finish();

    }


    private void stickyMessage() {

        EventBus.getDefault().register(this);
        EventBus.getDefault().removeStickyEvent(MessageBean.class);

    }

    @WxSubscribe(threadMode = ThreadMode.MAIN, sticky = true)
    public void sticky(MessageBean bean) {
        Log.e(Constants.TAG, "sticky   " + bean.toString());
        Log.e(Constants.TAG, "SecondActivity-->Thread Name:" + Thread.currentThread().getName());
        tvContent.setText(String.format("接收到的粘性消息：\n %s", bean.toString()));
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        // 示例代码
        MessageBean userInfo = EventBus.getDefault().getStickyEvent(MessageBean.class);
        if (userInfo != null) {
            MessageBean info = EventBus.getDefault().removeStickyEvent(MessageBean.class);
            if (info != null) {
                EventBus.getDefault().removeAllStickyEvents();
            }
        }
        EventBus.getDefault().unregister(this);
    }


}
