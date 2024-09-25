package com.zbinyds.easyflv.service;

import com.zbinyds.easyflv.service.impl.DefaultConverterThread;
import com.zbinyds.easyflv.service.impl.TranscodingConverterThread;
import com.zbinyds.easyflv.util.JavaCvUtil;
import org.bytedeco.ffmpeg.global.avcodec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.AsyncContext;
import javax.servlet.http.HttpServletRequest;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 转换器上下文。相当于本地缓存维度，提供了对于转换器对象的管理
 *
 * @Author zbinyds
 * @Create 2024-05-08 14:38
 */
public class ConverterContext {
    private static final Logger log = LoggerFactory.getLogger(ConverterContext.class);
    private static final Map<String, Converter> CONVERTER_BUFFER = new ConcurrentHashMap<>(128);

    public static Converter getConverter(String key) {
        return CONVERTER_BUFFER.get(key);
    }

    public static void register(String key, Converter converter) {
        CONVERTER_BUFFER.put(key, converter);
    }

    public static void remove(String key) {
        CONVERTER_BUFFER.remove(key);
    }

    public static Map<String, Converter> dumpAll() {
        return CONVERTER_BUFFER;
    }

    /**
     * 根据视频流格式，判断是否需要转码，然后创建对应的转换器
     *
     * @param url     待转换原始视频流（rtsp/rtmp）
     * @param key     url 唯一标识（md5）
     * @param request HttpServletRequest
     * @return 转换器
     */
    public static Converter generateAndRunning(String url, String key, HttpServletRequest request) {
        return Optional.ofNullable(JavaCvUtil.createGrabber(url))
                .map(grabber -> {
                    // 抓图器正常运行表明源视频流可用, 获取请求异步上下文对象, 转码器开始异步执行转码操作
                    AsyncContext context = request.startAsync();
                    context.setTimeout(0);
                    if (avcodec.AV_CODEC_ID_H264 == grabber.getVideoCodec()
                            && (grabber.getAudioChannels() == 0 || avcodec.AV_CODEC_ID_AAC == grabber.getAudioCodec())) {
                        // 创建默认转换器，H264+AAC
                        DefaultConverterThread defaultConverterThread = new DefaultConverterThread(key, url, context, grabber);
                        log.info("DefaultConverterThread 创建成功");
                        defaultConverterThread.start();
                        return defaultConverterThread;
                    } else {
                        // 创建转码转换器，需要转码为H264+AAC
                        TranscodingConverterThread transcodingConverterThread = new TranscodingConverterThread(key, url, context, grabber);
                        log.info("TranscodingConverterThread 创建成功");
                        transcodingConverterThread.start();
                        return transcodingConverterThread;
                    }
                }).orElse(null);
    }
}
