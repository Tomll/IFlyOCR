package com.drp.iflyocr;

import android.Manifest;
import android.app.Activity;
import android.app.ActivityManager;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.baidu.ocr.sdk.OCR;
import com.baidu.ocr.sdk.OnResultListener;
import com.baidu.ocr.sdk.exception.OCRError;
import com.baidu.ocr.sdk.model.AccessToken;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.Headers;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okio.BufferedSink;

public class MainActivity extends AppCompatActivity {


    OkHttpClient okHttpClient;
    Request request;
    String access_token;
    String url;
    FormBody formBody;
    String strBody;
    String urlImgStr;
    String encodeToBase64Str;
    String encodeToURLStr;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 68);
        }


    }


    @Override
    protected void onResume() {
        super.onResume();
        okHttpClient = new OkHttpClient();
        okHttpClient.retryOnConnectionFailure();

        //图片转为Base64编码,并进行URLEncode ,然后拼接成 body
        try {
            byte[] bytes = InputStream2ByteArray(Environment.getExternalStorageDirectory() + "/wangpeng.jpg");
            encodeToBase64Str = android.util.Base64.encodeToString(bytes, Base64.NO_WRAP);
            encodeToURLStr = URLEncoder.encode(encodeToBase64Str, "UTF-8");
            strBody = URLEncoder.encode("image", "UTF-8") + "=" + encodeToURLStr;
        } catch (IOException e) {
            Log.d("drp", e.toString());
            e.printStackTrace();
        }


        url = "http://webapi.xfyun.cn/v1/service/v1/ocr/business_card";
        String appid = "5b584faf";
        String curTime = String.valueOf(System.currentTimeMillis() / 1000);
        String param = "eyJlbmdpbmVfdHlwZSI6ICJidXNpbmVzc19jYXJkIn0=";
        String apiKey = "bd2dc5d9ce31614960edf357c58d7a50";
        String checkSum = md5(appid + curTime + apiKey);

        //请求内容实体body
//        formBody = new FormBody.Builder().add(parm, encodeToBase64Str).build();
        request = new Request.Builder()
                .url(url)
                //添加请求首部
                .addHeader("Content-Type", "application/x-www-form-urlencoded")
                .addHeader("X-Appid", appid)
                .addHeader("X-CurTime", curTime)
                .addHeader("X-Param", param)
                .addHeader("X-CheckSum", checkSum)
                //添加请求内容实体
                .post(RequestBody.create(MediaType.parse("application/x-www-form-urlencoded;charset=utf-8"), strBody))
//                .post(formBody)
                .build();

        okHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.d("drp", e.toString());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                Log.d("drp", "response.code():" + response.code());
                Log.d("drp", response.body().string());

            }
        });
    }

    /**
     * MD5 加密算法
     */
    public static String md5(String string) {
        byte[] hash;
        try {
            hash = MessageDigest.getInstance("MD5").digest(string.getBytes("UTF-8"));
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Huh, MD5 should be supported?", e);
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("Huh, UTF-8 should be supported?", e);
        }
        StringBuilder hex = new StringBuilder(hash.length * 2);
        for (byte b : hash) {
            if ((b & 0xFF) < 0x10) hex.append("0");
            hex.append(Integer.toHexString(b & 0xFF));
        }
        return hex.toString();
    }


    private byte[] InputStream2ByteArray(String filePath) throws IOException {

        InputStream in = new FileInputStream(filePath);
        byte[] data = toByteArray(in);
        in.close();
        return data;
    }

    private byte[] toByteArray(InputStream in) throws IOException {

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024 * 4];
        int n = 0;
        while ((n = in.read(buffer)) != -1) {
            out.write(buffer, 0, n);
        }
        return out.toByteArray();
    }


//    @Override
//    protected void onResume() {
//        super.onResume();
//
//        okHttpClient = new OkHttpClient();
//
//        //图片转为Base64编码
//        try {
//            byte[] bytes = InputStream2ByteArray(Environment.getExternalStorageDirectory() + "/wangpeng.jpg");
//
//            String encodeToString = android.util.Base64.encodeToString(bytes, android.util.Base64.NO_WRAP);
//            encodeToURLStr = URLEncoder.encode(encodeToString, "UTF-8");
//        } catch (IOException e) {
//            Log.d("drp", e.toString());
//            e.printStackTrace();
//        }
//
///*        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.mipmap.wangpeng);
//        ByteArrayOutputStream baos = new ByteArrayOutputStream();
//        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
//        byte[] datas = baos.toByteArray();
//        String base64Image = Base64.encode(datas);
//        try {
//            urlImgStr = URLEncoder.encode(base64Image, "UTF-8");
//        } catch (UnsupportedEncodingException e) {
//            e.printStackTrace();
//        }*/
//        String parm = null;
//        try {
//            parm = URLEncoder.encode("image", "UTF-8");
//        } catch (UnsupportedEncodingException e) {
//            e.printStackTrace();
//        }
//
//        //请求内容实体body
//        formBody = new FormBody.Builder().add(parm, encodeToURLStr).build();
//
//        //向百度服务器请求access_token,然后开始上传图片进行识别
//        new Thread(new Runnable() {
//            @Override
//            public void run() {
//                access_token = AuthService.getAuth();
//                Log.d("drp", "access_token: " + access_token);
//                url = "https://aip.baidubce.com/rest/2.0/ocr/v1/business_card?access_token="+access_token;
////                url = "https://aip.baidubce.com/rest/2.0/ocr/v1/general_basic?access_token=" + access_token;
//                Log.d("drp", "url: " + url);
//                request = new Request.Builder()
//                        .url(url)
//                        //添加请求首部
//                        .addHeader("Content-Type", "application/x-www-form-urlencoded")
//                        //添加请求内容实体
//                        .post(formBody)
//                        .build();
//
//
//                try {
//                    Response response = okHttpClient.newCall(request).execute();
//
//                    Log.d("drp", "response.code():" + response.code());
//                    Log.d("drp", "response.body().string(): " + response.body().string());
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//            }
//        }).start();
//
//
//    }
//
//    /**
//     * MD5 加密算法
//     */
//    public static String md5(String string) {
//        byte[] hash;
//        try {
//            hash = MessageDigest.getInstance("MD5").digest(string.getBytes("UTF-8"));
//        } catch (NoSuchAlgorithmException e) {
//            throw new RuntimeException("Huh, MD5 should be supported?", e);
//        } catch (UnsupportedEncodingException e) {
//            throw new RuntimeException("Huh, UTF-8 should be supported?", e);
//        }
//        StringBuilder hex = new StringBuilder(hash.length * 2);
//        for (byte b : hash) {
//            if ((b & 0xFF) < 0x10) hex.append("0");
//            hex.append(Integer.toHexString(b & 0xFF));
//        }
//        return hex.toString();
//    }
//
//
//    private byte[] InputStream2ByteArray(String filePath) throws IOException {
//
//        InputStream in = new FileInputStream(filePath);
//        byte[] data = toByteArray(in);
//        in.close();
//        return data;
//    }
//
//    private byte[] toByteArray(InputStream in) throws IOException {
//
//        ByteArrayOutputStream out = new ByteArrayOutputStream();
//        byte[] buffer = new byte[1024 * 4];
//        int n = 0;
//        while ((n = in.read(buffer)) != -1) {
//            out.write(buffer, 0, n);
//        }
//        return out.toByteArray();
//    }
//


}



