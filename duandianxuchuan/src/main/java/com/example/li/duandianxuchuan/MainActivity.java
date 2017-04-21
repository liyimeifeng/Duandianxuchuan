package com.example.li.duandianxuchuan;

import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import java.io.File;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action0;

public class MainActivity extends AppCompatActivity implements ProgressResponseBody.ProgressListener {

    public static final String TAG = "MainActivity";
//    public static final String PACKAGE_URL = "http://gdown.baidu.com/data/wisegame/02ba8a69a5a792b1/QQ_500.apk";
    public static final String URL = "http://10.10.30.235:9999/storage/emulated/0/movie.mp4";

    @Bind(R.id.progressBar)
    ProgressBar progressBar;
    private long breakPoints;
    private ProgressDownloader downloader;
    private File file;
    private long totalBytes;
    private long contentLength;
    private boolean isDownload = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
    }

    @OnClick({R.id.downloadButton, R.id.pause_button, R.id.continue_button,R.id.cancel_button})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.downloadButton:
                // 新下载前清空断点信息
//                if (isDownload){
//                    Toast.makeText(MainActivity.this, "已经下载", Toast.LENGTH_SHORT).show();
//                    return ;
//                }
                breakPoints = 0L;
                downloader = new ProgressDownloader(URL, getFile(URL), this);
                downloader.download(0L);
                break;
            case R.id.pause_button:
                downloader.pause();
                Toast.makeText(this, "下载暂停", Toast.LENGTH_SHORT).show();
                // 存储此时的totalBytes，即断点位置。
                breakPoints = totalBytes;
                break;
            case R.id.continue_button:
                downloader.download(breakPoints);
                break;
            case R.id.cancel_button:
                downloader.cancelDownload(getFile(URL));
                break;
        }
    }

    /**
     * 分割Url，得到文件名
     * @param url
     * @return
     */
    private File getFile(String url){
        String fileName=url.substring(url.lastIndexOf("/"));
        //下载文件存放的目录
        String directory= Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getPath();
        //创建一个文件
        file=new File(directory+fileName);
        return file;
    }

    @Override
    public void onPreExecute(long contentLength) {
        // 文件总长只需记录一次，要注意断点续传后的contentLength只是剩余部分的长度
        if (this.contentLength == 0L) {
            this.contentLength = contentLength;
            progressBar.setMax((int) (contentLength / 1024));
        }
    }

    @Override
    public void update(long totalBytes, boolean done) {
        // 注意加上断点的长度
//        Log.e(TAG, "totalBytes-----》 "+ totalBytes + "-----"  + breakPoints);
        this.totalBytes = totalBytes + breakPoints;
        progressBar.setProgress((int) (totalBytes + breakPoints) / 1024);
        if (done) {
            // 切换到主线程
            Observable
                    .empty()
                    .observeOn(AndroidSchedulers.mainThread())
                    .doOnCompleted(new Action0() {
                        @Override
                        public void call() {
                            Toast.makeText(MainActivity.this, "下载完成", Toast.LENGTH_SHORT).show();
                            isDownload = true;
                        }
                    })
                    .subscribe();
        }
    }
}
