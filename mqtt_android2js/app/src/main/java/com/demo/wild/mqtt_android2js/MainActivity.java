package com.demo.wild.mqtt_android2js;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.io.UnsupportedEncodingException;

public class MainActivity extends AppCompatActivity {

    final String topic = "MyTopic/0001";

    final String TAG = "mqtt";
    MqttAndroidClient client = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        ((Button)findViewById(R.id.btn_conn)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String uri = ((EditText)findViewById(R.id.et_uri)).getText().toString();
                String port = ((EditText)findViewById(R.id.et_port)).getText().toString();
                if(uri.length() > 0 && port.length() > 0){
                    initMqtt(uri + ":" + port);
                }
            }
        });

        ((Button)findViewById(R.id.btn_up)).setOnTouchListener(new View.OnTouchListener(){

            @Override
            public boolean onTouch(View v, MotionEvent event) {

                mqttPublish("top");
                return true;
            }
        });

        ((Button)findViewById(R.id.btn_down)).setOnTouchListener(new View.OnTouchListener(){

            @Override
            public boolean onTouch(View v, MotionEvent event) {

                mqttPublish("bottom");
                return true;
            }
        });

        ((Button)findViewById(R.id.btn_left)).setOnTouchListener(new View.OnTouchListener(){

            @Override
            public boolean onTouch(View v, MotionEvent event) {

                mqttPublish("left");
                return true;
            }
        });

        ((Button)findViewById(R.id.btn_right)).setOnTouchListener(new View.OnTouchListener(){

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                mqttPublish("right");
                return true;
            }
        });


        ((Button)findViewById(R.id.btn_canvas)).setOnTouchListener(new View.OnTouchListener(){

            @Override
            public boolean onTouch(View v, MotionEvent event) {

                switch (event.getActionMasked()) {
                    case MotionEvent.ACTION_MOVE:
                        float finalX = event.getX();
                        float finalY = event.getY();
                        Log.d(TAG, "Action was MOVE, X=" + finalX + ", Y=" + finalY);

                        mqttPublish(finalX + "|" + finalY);
                }
                return true;
            }
        });
    }

    private void initMqtt(final String serverURI){

        String clientId = MqttClient.generateClientId();
        client =
                new MqttAndroidClient(this.getApplicationContext(), "tcp://" + serverURI, clientId);

        try {
            MqttConnectOptions opt = new MqttConnectOptions();
            /*
            opt.setKeepAliveInterval(30);
            opt.setUserName(userName);
            opt.setPassword(password.toCharArray());
            opt.setKeepAliveInterval(30);
            */
            opt.setConnectionTimeout(1);
            IMqttToken token = client.connect(opt);
            token.setActionCallback(new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    // We are connected
                    Log.d(TAG, "onSuccess");
                    Toast.makeText(MainActivity.this, "Connect Success", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    // Something went wrong e.g. connection timeout or firewall problems
                    Log.d(TAG, "onFailure");
                    Toast.makeText(MainActivity.this, "Connect " + serverURI + " failed", Toast.LENGTH_SHORT).show();
                }
            });
        } catch (MqttException e) {
            e.printStackTrace();
        }

    }

    private void mqttPublish(String msg){
        if(client == null || ! client.isConnected()){
            return;
        }
        byte[] encodedPayload = new byte[0];
        try {
            encodedPayload = msg.getBytes("UTF-8");
            MqttMessage message = new MqttMessage(encodedPayload);
            message.setRetained(true);
            client.publish(topic, message);
        } catch (UnsupportedEncodingException | MqttException e) {
            e.printStackTrace();
        }
    }
}
