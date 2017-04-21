package com.example.li.duandianxuchuan;

import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Headers;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

/**
 * Created by Lee on 2017/4/7 0007.
 * 带进度监听功能的辅助类
 */
public class ProgressDownloader {

    public static final String TAG = "ProgressDownloader";
    private ProgressResponseBody.ProgressListener progressListener;
    private String url;
    private OkHttpClient client;
    private File file;
    private Call call;
    private long totalLength;

    public ProgressDownloader(String url, File destination, ProgressResponseBody.ProgressListener progressListener) {
        this.url = url;
        this.file = destination;
        this.progressListener = progressListener;
        //在下载、暂停后的继续下载中可复用同一个client对象
        client = getProgressClient();
    }
    //每次下载需要新建新的Call对象
    private Call newCall(long startPoints) {
        Request request = new Request.Builder()
                .url(url)
                .header("RANGE", "bytes=" + startPoints + "-")//断点续传要用到的，指示下载的区间
//                .addHeader("RANGE", "bytes=" + startPoints + "-" + totalLength)
                .build();
        return client.newCall(request);
    }

    public OkHttpClient getProgressClient() {
        // 拦截器，用上ProgressResponseBody
        Interceptor interceptor = new Interceptor() {
            @Override
            public Response intercept(Chain chain) throws IOException {
                Response originalResponse = chain.proceed(chain.request());
                return originalResponse.newBuilder()
                        .body(new ProgressResponseBody(originalResponse.body(), progressListener))
                        .build();
            }
        };

        return new OkHttpClient.Builder()
                .addNetworkInterceptor(interceptor)
                .build();
    }

    // startsPoint指定开始下载的点
    public void download(final long startsPoint) {
        Log.e(TAG, "断点startsPoint=========>" + startsPoint );
            call = newCall(startsPoint);
            call.enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {

                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    save(response, startsPoint);
                    totalLength =  response.body().contentLength();
                }
            });
    }

    public void pause() {
        if(call!=null){
            call.cancel();
        }
    }

    public  void cancelDownload(File file){
        if (file.exists()){
            if (call != null){
                call.cancel();
            }
            file.delete();
        }
    }

//    private void save(Response response, long startsPoint) {
//        ResponseBody body = response.body();
//        InputStream in = body.byteStream();
//        FileChannel channelOut = null;
//        // 随机访问文件，可以指定断点续传的起始位置
//        RandomAccessFile randomAccessFile = null;
//        try {
//            randomAccessFile = new RandomAccessFile(file, "rwd");
////            randomAccessFile.seek(startsPoint);//跳过已经下载的字节
//            //Chanel NIO中的用法，由于RandomAccessFile没有使用缓存策略，直接使用会使得下载速度变慢，亲测缓存下载3.3秒的文件，用普通的RandomAccessFile需要20多秒。
//            // 但是会造成文件刚建立就有源文件一样的大小
//            channelOut = randomAccessFile.getChannel();
//            // 内存映射，直接使用RandomAccessFile，是用其seek方法指定下载的起始位置，使用缓存下载，在这里指定下载位置。
//            MappedByteBuffer mappedBuffer = channelOut.map(FileChannel.MapMode.READ_WRITE, startsPoint, body.contentLength());
//            byte[] buffer = new byte[1024];
//            int len;
//            while ((len = in.read(buffer)) != -1) {
//                mappedBuffer.put(buffer, 0, len);
//            }
//        } catch (IOException e) {
//            e.printStackTrace();
//        }finally {
//            try {
//                in.close();
//                if (channelOut != null) {
//                    channelOut.close();
//                }
//                if (randomAccessFile != null) {
//                    randomAccessFile.close();
//                }
//            } catch (IOException e) {
//                e.printStackTrace();
//        }
//            }
//    }

    /**
     * 效果最佳的实现方式
     * @param response
     * @param startsPoint
     */
    private void save(Response response, long startsPoint) {
        ResponseBody body = response.body();
        InputStream in = body.byteStream();
        FileChannel channelOut = null;
        // 随机访问文件，可以指定断点续传的起始位置
        RandomAccessFile randomAccessFile = null;
        try {
            /**
             * 测试
             */
//            String rangeHeader = response.header("RANGE");
//            Log.e(TAG, "响应头得到下载范围---------》" + rangeHeader );
             Headers headers = response.headers();
            Log.e(TAG, "打印出所有headers------> " +headers );

            in=response.body().byteStream();
            randomAccessFile=new RandomAccessFile(file,"rwd");
            byte[] buffer=new byte[1024];
            int total=0;
            int len;
            while((len=in.read(buffer))!=-1){

                    total+=len;
                randomAccessFile.write(buffer,0,len);
            }
            response.body().close();
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            try {
                in.close();
                if (channelOut != null) {
                    channelOut.close();
                }
                if (randomAccessFile != null) {
                    randomAccessFile.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}