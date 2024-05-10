package com.zbinyds.easyflv.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.AsyncContext;
import java.io.Closeable;
import java.io.Flushable;
import java.io.IOException;

public interface Converter {
    Logger log = LoggerFactory.getLogger(Converter.class);

    /**
     * 获取该转换的key
     */
    String getKey();

    /**
     * 获取该转换的url
     */
    String getUrl();

    /**
     * 添加一个流输出，相当于添加一个客户端播放视频流（一个rtsp流兼容多端播放的情况）
     *
     * @param key    key
     * @param entity 客户端异步上下文
     */
    void addOutputStreamEntity(String key, AsyncContext entity) throws IOException;

    /**
     * 退出转换
     */
    void exit();

    /**
     * 关闭流<br />
     * 忽略异常关闭
     *
     * @param closeable 不能为空
     */
    default void closeStream(Closeable closeable) {
        if (closeable != null) {
            try {
                if (closeable instanceof Flushable) {
                    // 先刷新流
                    ((Flushable) closeable).flush();
                }
                closeable.close();
            } catch (IOException ignored) {
                // ignore
            }
        }
    }
}