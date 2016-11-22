package com.solu.toolbarserver;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.io.IOException;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {
    BluetoothAdapter bluetoothAdapter;
    static final int REQUEST_BLUETOOTH_ENABLE=1;
    static final int REQUEST_DISCOVERABLE=2;

    TextView txt_status;
    /*클라이언트는 이 UUID 를 통해서 나의 서버로 접속
    * 하면 된다..*/
    String UUID="8dd5677d-b88a-4683-92a1-8755887f0a93";

    /*클라이언트의 접속을 받을 수 있는 서버(소켓서버와 상당히 비슷)*/
    BluetoothServerSocket server;
    String serviceName;
    Thread acceptThread; /*접속자를 받기위한 쓰레드*/
    Handler handler;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        txt_status=(TextView)findViewById(R.id.txt_status);

        handler = new Handler(){
            /*UI제어가 가능하다...왜  main Thread에서 호출*/
            public void handleMessage(Message message) {
                Bundle bundle=message.getData();
                String msg=bundle.getString("msg");
                txt_status.append(msg);
            }
        };

        checkSupportBluetooth();
        requestActiveBluetooth();
        requestDiscoverable();
        acceptDevice();
    }

    /*--------------------------------------------------
     이 디바이스가 블루투스를 지원하는 지 여부 체크
     --------------------------------------------------*/
    public void checkSupportBluetooth(){
        bluetoothAdapter=BluetoothAdapter.getDefaultAdapter();
        if(bluetoothAdapter==null){
            showMsg("안내","이 디바이스는 블루투스를 지원하지 않습니다.");
            this.finish(); /*현재 액티비티를 닫는다*/
        }
    }

    /*--------------------------------------------------
     유저에게 활성화 요청
     --------------------------------------------------*/
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch(requestCode){
            case REQUEST_BLUETOOTH_ENABLE:
                if(resultCode==RESULT_CANCELED){
                    showMsg("경고","앱을 사용하려면 블루투스를 활성화 해야 합니다.");
                }break;
            case REQUEST_DISCOVERABLE:
                if(resultCode==RESULT_CANCELED){
                    showMsg("경고","발견되게 해주세요!!");
                }
        }
    }
    public void requestActiveBluetooth(){
        Intent intent = new Intent();
        intent.setAction(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        startActivityForResult(intent, REQUEST_BLUETOOTH_ENABLE);
    }

    /*접속자 받을 준비*/
    public void acceptDevice(){
        serviceName=this.getPackageName();
        try {
            server = bluetoothAdapter.listenUsingRfcommWithServiceRecord(serviceName, java.util.UUID.fromString(UUID));
        } catch (IOException e) {
            e.printStackTrace();
        }

        acceptThread = new Thread(){
            public void run() {
                try {
                    Message message=new Message();
                    Bundle bundle = new Bundle();
                    bundle.putString("msg", "서버준비됨\n");
                    message.setData(bundle);
                    handler.sendMessage(message);

                    BluetoothSocket socket=server.accept();

                    ServerThread st=new ServerThread(socket);
                    st.start();/*클라이언트의 말 청취 시작!!*/

                    /*
                     더이상 접속자 허용방지 !!
                     서버를 프로세스를 중단하는 것이 아니라,
                     접속자의 접속을 원천 차단이 목적임!! No 오해!
                     */
                    server.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        };
        acceptThread.start();
    }
    /*클라이언트가 서버인 나를 발견할 수 있도록,
       검색허용 옵션을 지정하자..
    */
    public void requestDiscoverable(){
        Intent intent = new Intent();
        intent.setAction(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
        intent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 60*3);
        startActivityForResult(intent, REQUEST_DISCOVERABLE);
    }

    /*대화나누기*/


    public void showMsg(String title, String msg){
        AlertDialog.Builder alert=new AlertDialog.Builder(this);
        alert.setTitle(title).setMessage(msg).show();
    }

}
