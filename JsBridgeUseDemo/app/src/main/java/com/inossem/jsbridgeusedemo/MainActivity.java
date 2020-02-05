package com.inossem.jsbridgeusedemo;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.github.lzyzsd.jsbridge.BridgeHandler;
import com.github.lzyzsd.jsbridge.BridgeWebView;
import com.github.lzyzsd.jsbridge.CallBackFunction;
import com.github.lzyzsd.jsbridge.DefaultHandler;

public class MainActivity extends AppCompatActivity {

    //	一.使用 com.github.lzyzsd.jsbridge.BridgeWebView 代替 WebView.
    BridgeWebView webView;

    Button button2, button1;

    int RESULT_CODE = 0;

    ValueCallback<Uri> mUploadMessage;

    ValueCallback<Uri[]> mUploadMessageArray;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        webView = (BridgeWebView) findViewById(R.id.webView);
        button2 = (Button) findViewById(R.id.button2);
        button1 = (Button) findViewById(R.id.button1);

//        webView.getSettings().setDefaultTextEncodingName("UTF-8");
//        WebSettings webSettings = webView.getSettings();
//        webSettings.setJavaScriptEnabled(true);//允许使用js
////        webSettings.setUseWideViewPort(true);  // WebView屏幕自适应
//        webSettings.setLoadWithOverviewMode(true);
//        webSettings.setDomStorageEnabled(true);


        // 文件相关
        webView.setWebChromeClient(new WebChromeClient() {

            @SuppressWarnings("unused")
            public void openFileChooser(ValueCallback<Uri> uploadMsg, String AcceptType, String capture) {
                this.openFileChooser(uploadMsg);
            }

            @SuppressWarnings("unused")
            public void openFileChooser(ValueCallback<Uri> uploadMsg, String AcceptType) {
                this.openFileChooser(uploadMsg);
            }

            public void openFileChooser(ValueCallback<Uri> uploadMsg) {
                mUploadMessage = uploadMsg;
                pickFile();
            }

            @Override
            public boolean onShowFileChooser(WebView webView, ValueCallback<Uri[]> filePathCallback, FileChooserParams fileChooserParams) {
                mUploadMessageArray = filePathCallback;
                pickFile();
                return true;
            }

            //加载进度回调
            @Override
            public void onProgressChanged(WebView view, int newProgress) {
//                progressBar.setProgress(newProgress);
            }


        });


        /*  如果加上下面的代码  自己写的js交互就失效了 */
//        webView.setWebViewClient(new WebViewClient(){
//            @Override
//            public void onPageFinished(WebView view, String url) {//页面加载完成
//                progressBar.setVisibility(View.GONE);
//                super.onPageFinished(view, url);
//            }
//
//            @Override
//            public void onPageStarted(WebView view, String url, Bitmap favicon) {//页面开始加载
//                progressBar.setVisibility(View.VISIBLE);
//                super.onPageStarted(view, url, favicon);
//            }
//
//            @Override
//            public boolean shouldOverrideUrlLoading(WebView view, String url) {
//                try {
//                    if (!url.startsWith("http:") ||!url.startsWith("https:")) {
//                        Intent intent = new Intent(Intent.ACTION_VIEW,
//                                Uri.parse(url));
//                        startActivity(intent);
//                        return true;
//                    }
//                }
//                catch (Exception e){
//                    return false;
//                }
//
//                view.loadUrl(url);
//                return true;
//            }
//        });


        // -----------------      原生给js发消息方法 （默认）  两种  有回调无回调    ----------------------
        button1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                webView.send("我是原生给js发的默认消息");
                webView.send("我是原生给js发的默认消息", new CallBackFunction() {
                    @Override
                    public void onCallBack(String data) {
                        Toast.makeText(MainActivity.this, data + "", Toast.LENGTH_LONG).show();
                    }
                });
            }
        });


        //  -----------------      原生调用js方法    ----------------------
        button2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //  原生调用js方法
                webView.callHandler("functionInJs", "我是原生给js传的信息", new CallBackFunction() {
                    @Override
                    public void onCallBack(String data) {  //  从 js返回的回调结果
                        Toast.makeText(MainActivity.this, data + "", Toast.LENGTH_LONG).show();
                    }
                });
            }
        });


        //   -----------------      js 调用  原生  （默认）   两种：有回调无回调    ----------------------
        webView.setDefaultHandler(new DefaultHandler());
        webView.setDefaultHandler(new DefaultHandler() {
            @Override
            public void handler(String data, CallBackFunction function) {
                super.handler(data, function);

                Toast.makeText(MainActivity.this, data, Toast.LENGTH_LONG).show();

                // 下面的回调是传不过去的 因为 DefaultHandler 代码中固定返回的一个字符串，所以下面这句话没有用
                function.onCallBack("我是js调用原生之后，原生给js的返回值（默认）");
                // DefaultHandler 中的处理方式
                // if(function != null){
                //			function.onCallBack("DefaultHandler response data");
                //		}
            }
        });


        //  -----------------      js 调用  原生     ----------------------
        webView.registerHandler("submitFromWeb", new BridgeHandler() {
            @Override
            public void handler(String data, CallBackFunction function) {  //  data 为js给原生传过来的
                Toast.makeText(MainActivity.this, data, Toast.LENGTH_LONG).show();
                function.onCallBack("我是js调用原生之后，原生给js的返回值");  //  原生给 js 返回的结果
            }
        });


        webView.loadUrl("file:///android_asset/demo.html");
    }


    public void pickFile() {
        Intent chooserIntent = new Intent(Intent.ACTION_GET_CONTENT);
        chooserIntent.setType("image/*");
        startActivityForResult(chooserIntent, RESULT_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        if (requestCode == RESULT_CODE) {
            if (null == mUploadMessage && null == mUploadMessageArray) {
                return;
            }
            if (null != mUploadMessage && null == mUploadMessageArray) {
                Uri result = intent == null || resultCode != RESULT_OK ? null : intent.getData();
                mUploadMessage.onReceiveValue(result);
                mUploadMessage = null;
            }

            if (null == mUploadMessage && null != mUploadMessageArray) {
                Uri result = intent == null || resultCode != RESULT_OK ? null : intent.getData();
                mUploadMessageArray.onReceiveValue(new Uri[]{result});
                mUploadMessageArray = null;
            }

        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (webView != null) {
            //释放资源
            webView.loadDataWithBaseURL(null, "", "text/html", "utf-8", null);
            webView.clearHistory();
            ((ViewGroup) webView.getParent()).removeView(webView);

            webView.setWebChromeClient(null);
            webView.setWebViewClient(null);
            webView.getSettings().setJavaScriptEnabled(false);
            webView.clearCache(true);
            webView.removeAllViews();
            webView.destroy();

        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if (webView.canGoBack()) {
            webView.getSettings().setCacheMode(WebSettings.LOAD_NO_CACHE);
            webView.goBack();
        }
    }


}
