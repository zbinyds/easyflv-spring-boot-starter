package com.zbinyds.easyflv.service;

import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.FFmpegFrameRecorder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.AsyncContext;
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.Flushable;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;

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
     * 启动
     */
    void start();

    /**
     * 退出转换
     */
    void exit();

    /**
     * 输出FLV视频流
     *
     * @param outs 客户端异步上下文列表
     * @param b    等待输出的视频流数据
     */
    default void writeResponse(List<AsyncContext> outs, byte[] b) {
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
     * @param key      url-md5加密，唯一标识
     */
    default void safeClose(FFmpegFrameGrabber grabber, FFmpegFrameRecorder recorder, ByteArrayOutputStream stream,
                           List<AsyncContext> outs, String key) {
        log.info("转换器[{}]安全关闭", key);
        closeStream(grabber);
        closeStream(recorder);
        closeStream(stream);
        ConverterContext.remove(key);
        outs.forEach(AsyncContext::complete);
    }

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