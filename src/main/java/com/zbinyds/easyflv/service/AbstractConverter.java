package com.zbinyds.easyflv.service;

import javax.servlet.AsyncContext;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @Author zbinyds
 * @Create 2024-05-09 10:02
 */
public abstract class AbstractConverter extends Thread implements Converter {
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
    public void addOutputStreamEntity(String key, AsyncContext entity) throws IOException {
        if (this.headers != null) {
            // 如果有第二个客户端播放首先要返回头信息
            entity.getResponse().getOutputStream().write(headers);
            entity.getResponse().getOutputStream().flush();
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
}
