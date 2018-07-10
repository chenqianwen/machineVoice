package com.example.voice;

import com.alibaba.idst.nls.NlsClient;
import com.alibaba.idst.nls.NlsFuture;
import com.alibaba.idst.nls.event.NlsEvent;
import com.alibaba.idst.nls.event.NlsListener;
import com.alibaba.idst.nls.protocol.NlsRequest;
import com.alibaba.idst.nls.protocol.NlsResponse;

import java.io.File;
import java.io.FileOutputStream;

/**
 * @author： ygl
 * @date： 2018/07/10
 * @Description：
 */
public class TTSDemo implements NlsListener {
    private NlsClient client = new NlsClient();
    public TTSDemo() {
        System.out.println("init Nls client...");
        // 初始化NlsClient
        client.init();
    }
    public void shutDown() {
        System.out.println("close NLS client");
        // 关闭客户端并释放资源
        client.close();
        System.out.println("demo done");
    }
    public void startTTS() {
        File file = new File("tts.wav");
        if(!file.exists()) {
            try {
                file.createNewFile();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        NlsRequest req = new NlsRequest();
        String appkey = "nls-service";
        req.setAppKey(appkey); // 设置语音文件格式
        req.setTtsRequest("您的患者林俊杰，预约时间13:30。已经到店。"); //传入测试文本，返回语音结果
        req.setTtsEncodeType("wav");//返回语音数据格式，支持pcm,wav.alaw
        req.setTtsVolume(30);       //音量大小默认50，阈值0-100
        req.setTtsSpeechRate(0);    //语速，阈值-500~500
        req.setTtsBackgroundMusic(1);//背景音乐编号
        req.authorize("LTAIJ1D41kwmvg2Q", "uIQk24KSjspCMf5WpQmbsANuDpN5uj"); // 请替换为用户申请到的Access Key ID和Access Key Secret
        try {
            FileOutputStream fileOutputStream = new FileOutputStream(file);
            NlsFuture future = client.createNlsFuture(req, this); // 实例化请求,传入请求和监听器
            int total_len = 0;
            byte[] data ;
            while((data = future.read()) != null) {
                fileOutputStream.write(data, 0, data.length);
                total_len += data.length;
                System.out.println("tts length " + data.length);
            }
            fileOutputStream.close();
            System.out.println("tts audio file size is :" + total_len);
            future.await(10000); // 设置服务端结果返回的超时时间
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    @Override
    public void onMessageReceived(NlsEvent e) {
        NlsResponse response = e.getResponse();
        String result = "";
        int statusCode = response.getStatus_code();
        if (response.getTts_ret() != null) {
            result += "\nget tts result: statusCode=[" + statusCode + "], " + response.getTts_ret();
        }
        if (result != null) {
            System.out.println(result);
        }
        else {
            System.out.println(response.jsonResults.toString());
        }
    }
    @Override
    public void onOperationFailed(NlsEvent e) {
        //合成失败的回调
        System.out.print("on operation failed: ");
        System.out.println(e.getErrorMessage());
    }
    @Override
    public void onChannelClosed(NlsEvent e) {
        //socket 连接关闭的回调
        System.out.println("on websocket closed.");
    }
    /**
     * @param args
     */
    public static void main(String[] args) {
        TTSDemo ttsDemo = new TTSDemo();
        ttsDemo.startTTS();
        ttsDemo.shutDown();
    }
}