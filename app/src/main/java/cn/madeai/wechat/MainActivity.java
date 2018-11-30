package cn.madeai.wechat;

import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {
    private Button button;
    private EditText editMsg;
    private ListView content;
    private Socket socket;
    private long start=0L;
    private String exmsg="";
//    private ExecutorService threadPool;
    private List<String> list=new ArrayList<>();
    private Handler handler;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        button=findViewById(R.id.button);
        editMsg=findViewById(R.id.msg);
        content=findViewById(R.id.content);
        //content.setSelection(content.getBottom());
//        threadPool = Executors.newCachedThreadPool();
        new Thread(){
            @Override
            public void run() {
                super.run();
                connectServer();
                readMsg();
            }
        }.start();
        //利用handler来进行消息传递,通过handler来传递message,以此更新UI(ListView)
        handler = new Handler() {
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                switch (msg.what) {
                    case 0:
                        content.setAdapter(new ArrayAdapter<String>(MainActivity.this,android.R.layout.simple_list_item_1,list));
                        break;
                    case 1:
                        showDialog(exmsg);
                }
            }
        };
//        threadPool.execute(new Runnable() {
//            @Override
//            public void run() {
//                connectServer();
//            }
//        });
//        threadPool.execute(new Runnable() {
//            @Override
//            public void run() {
//                readMsg();
//            }
//        });
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(editMsg.getText().length()!=0){
                    if((System.currentTimeMillis()-start)<1000) {
                        Snackbar.make(v, "输入速度太快,疑似暴力输入.", Snackbar.LENGTH_SHORT)
                                .setAction("下次不会了/(ㄒoㄒ)/~~", new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        //Toast.makeText(EditActivity.this,"Data unsored",Toast.LENGTH_SHORT).show();
                                    }
                                })
                                .show();
                        return;
                    }
                    start=System.currentTimeMillis();
                    //耗时的操作放在子线程中...
                    new Thread() {
                        @Override
                        public void run() {
                            super.run();
                            sendMsg(editMsg.getText().toString().trim());
                        }
                    }.start();
                    editMsg.setText("");
                }else{
                    Toast.makeText(MainActivity.this, "输入内容为空!!", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
//                threadPool.execute(new Runnable() {
//                    @Override
//                    public void run() {
//                        String message=editMsg.getText().toString().trim();
//                        //socket.isConnected()&&!socket.isOutputShutdown()
//                        if(!"".equals(editMsg)) {
//                            sendMsg(message);
//                            editMsg.setText("");
//                        }else {
//                            Toast.makeText(MainActivity.this, "输入内容为空!!", Toast.LENGTH_SHORT).show();
//                        }
//                    }
//                });
    public void connectServer() {
        //192.168.0.185
        try {
            socket=new Socket("47.94.243.176",7777);
        } catch (UnknownHostException e) {
            // TODO Auto-generated catch block
            exmsg="未知主机异常：" + e.getMessage();
            Message message=Message.obtain();
            message.what=1;
            handler.sendMessage(message);
            //e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            //e.printStackTrace();
            exmsg="IO异常：" + e.getMessage();
            Message message=Message.obtain();
            message.what=1;
            handler.sendMessage(message);
        }
    }

    public void showDialog(String msg) {
        new AlertDialog.Builder(this).setTitle("通知").setMessage(msg)
                .setPositiveButton("ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                }).show();
    }

    /**
     * 发送消息
     * @param str 消息内容
     * indexOf("hello")
     * message=message.replace("hello","****");
     */

    public void sendMsg(String str) {
        try {
            //获取输出流;
            OutputStream os = socket.getOutputStream();
            //将输出流转化为dataoutputstream
            DataOutputStream dos=new DataOutputStream(os);
            //向服务器发送消息;
            dos.writeUTF(str);
        } catch (IOException e) {
            // 连接断开;
            e.printStackTrace();
        }
    }
    public void readMsg() {
        try {
            InputStream is= socket.getInputStream();
            DataInputStream dis=new DataInputStream(is);
            //定义一个临时变量接收服务器的消息;
            String msg=null;
            //监听服务器发送的消息;
            while((msg=dis.readUTF(dis))!=null){
                //读取到了消息;
                list.add(msg);
                Message message=Message.obtain();
                message.what=0;
                handler.sendMessage(message);
            }
            //关闭流:服务器已经关闭;
            dis.close();
            is.close();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }
}
