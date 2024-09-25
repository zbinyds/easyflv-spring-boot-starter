package com.zbinyds.easyflv.service;

import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.FFmpegFrameRecorder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.AsyncContext;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * @Author zbinyds
 * @Create 2024-05-09 10:02
 */
public abstract class AbstractConverter extends Thread implements Converter {
    private static final Logger log = LoggerFactory.getLogger(AbstractConverter.class);
    public volatile boolean isRunning = true;

    /**
     * md5-url，唯一key
     */
    private final String key;

    /**
     * rtsp-url
     */
    private final String url;

    /**
     * 客户端异步上下文集合，一个rtsp流可以有多个客户端播放
     */
    private final List<AsyncContext> outs = new ArrayList<>();

    /**
     * 转FLV格式的头信息<br/>
     * 如果有第二个客户端播放首先要返回头信息
     */
    private byte[] headers;

    public AbstractConverter(String key, String url, AsyncContext context) {
        this.key = key;
        this.url = url;
        this.outs.add(context);
    }

    public List<AsyncContext> getOuts() {
        return outs;
    }

    public byte[] getHeaders() {
        return headers;
    }

    public void setHeaders(byte[] headers) {
        this.headers = headers;
    }

    @Override
    public String getKey() {
        return this.key;
    }

    @Override
    public String getUrl() {
        return this.url;
    }

    @Override
    public void addOutputStreamEntity(AsyncContext entity) {
        try {
            if (this.headers != null) {
                // 如果有第二个客户端播放首先要返回头信息
                entity.getResponse().getOutputStream().write(headers);
                entity.getResponse().getOutputStream().flush();
            }
        } catch (Exception e) {
            // 异常忽略此客户端播放
            log.info("error msg: {}", e.getMessage(), e);
            return;
        }
        outs.add(entity);
    }

    @Override
    public void exit() {
        this.isRunning = false;
        try {
            this.join();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }

    /**
     * 输出FLV视频流
     *
     * @param outs 客户端异步上下文列表
     * @param b    等待输出的视频流数据
     */
    protected void writeResponse(List<AsyncContext> outs, byte[] b) {
        Iterator<AsyncContext> it = outs.iterator();
        while (it.hasNext()) {
            AsyncContext o = it.next();
            try {
                o.getResponse().getOutputStream().write(b);
            } catch (Exception e) {
                log.info("移除一个客户端输出");
                it.remove();
            }
        }
    }

    /**
     * 关闭转换
     *
     * @param grabber  抓图器
     * @param recorder 转码器
     * @param stream   保存的视频流数据
     * @param outs     客户端异步上下文列表
     */
    protected void safeClose(FFmpegFrameGrabber grabber, FFmpegFrameRecorder recorder, ByteArrayOutputStream stream,
                             List<AsyncContext> outs) {
        closeStream(grabber);
        closeStream(recorder);
        closeStream(stream);
        ConverterContext.remove(this.getKey());
        outs.forEach(AsyncContext::complete);
    }
}
