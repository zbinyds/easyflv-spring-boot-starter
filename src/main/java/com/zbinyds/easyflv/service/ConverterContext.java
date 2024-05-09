package com.zbinyds.easyflv.service;

import com.zbinyds.easyflv.service.impl.DefaultConverterThread;
import com.zbinyds.easyflv.service.impl.TranscodingConverterThread;
import com.zbinyds.easyflv.util.JavaCvUtil;
import org.bytedeco.ffmpeg.global.avcodec;
import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.AsyncContext;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @Author zbinyds
 * @Create 2024-05-08 14:38
 */
public class ConverterContext {
    private static final Logger log = LoggerFactory.getLogger(ConverterContext.class);
    private static final Map<String, Converter> CACHE = new ConcurrentHashMap<>();

    public static Converter getConverter(String key) {
        return CACHE.get(key);
    }

    public static void register(String key, Converter converter) {
        CACHE.put(key, converter);
    }

    public static void remove(String key) {
        CACHE.remove(key);
    }

    public static Converter generateAndRunning(String url, String key, AsyncContext context) {
        FFmpegFrameGrabber grabber = JavaCvUtil.createGrabber(url);
        if (avcodec.AV_CODEC_ID_H264 == grabber.getVideoCodec()
                && (grabber.getAudioChannels() == 0 || avcodec.AV_CODEC_ID_AAC == grabber.getAudioCodec())) {
            // 创建默认转换器，H264+AAC
            DefaultConverterThread defaultConverterThread = new DefaultConverterThread(key, url, context);
            log.info("DefaultConverterThread 创建成功");
            defaultConverterThread.start();
            return defaultConverterThread;
        } else {
            // 创建转码转换器，需要转码为H264+AAC
            TranscodingConverterThread transcodingConverterThread = new TranscodingConverterThread(key, url, context);
            log.info("TranscodingConverterThread 创建成功");
            transcodingConverterThread.start();
            return transcodingConverterThread;
        }
    }
}
