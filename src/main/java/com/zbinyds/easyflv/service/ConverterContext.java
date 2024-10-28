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
import java.util.concurrent.*;

/**
 * 转换器上下文。相当于本地缓存维度，提供了对于转换器对象的管理
 *
 * @Author zbinyds
 * @Create 2024-05-08 14:38
 */
public final class ConverterContext {
    private static final Logger log = LoggerFactory.getLogger(ConverterContext.class);
    private static final Map<String, Converter> CONVERTER_BUFFER = new ConcurrentHashMap<>(2 << 6);
    private static final ThreadPoolExecutor CONVERT_POOL = new ThreadPoolExecutorMonitor(
            10,
            10,
            0,
            TimeUnit.SECONDS,
            new LinkedBlockingQueue<>(2 << 6),
            ThreadPoolExecutorMonitor.monitorThreadFactory("flv"),
            new ThreadPoolExecutor.AbortPolicy()
    );

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
     * @param context 请求异步上下文对象
     * @return 转换器
     */
    public static Converter generateAndRunning(String url, String key, AsyncContext context) {
        try {
            FFmpegFrameGrabber grabber = JavaCvUtil.createGrabber(url);
            grabber.start();

            if (avcodec.AV_CODEC_ID_H264 == grabber.getVideoCodec()
                    && (grabber.getAudioChannels() == 0 || avcodec.AV_CODEC_ID_AAC == grabber.getAudioCodec())) {
                // 创建默认转换器，H264+AAC/无音频
                DefaultConverterThread defaultConverterThread = new DefaultConverterThread(key, url, context, grabber);
                CONVERT_POOL.execute(defaultConverterThread);
                return defaultConverterThread;
            } else {
                // 创建转码转换器，需要转码为H264+AAC
                TranscodingConverterThread transcodingConverterThread = new TranscodingConverterThread(key, url, context, grabber);
                CONVERT_POOL.execute(transcodingConverterThread);
                return transcodingConverterThread;
            }
        } catch (FFmpegFrameGrabber.Exception e) {
            log.error("运行抓图器FFmpegFrameGrabber失败, error: {}", e.getMessage(), e);
            return null;
        } catch (RejectedExecutionException e) {
            // 线程池执行失败, 兜底返回null客户端直接失败无需等待
            log.error("线程池已达上限拒绝执行, msg: {}", e.getMessage(), e);
            return null;
        } catch (Exception e) {
            // 未知
            log.error("未知异常", e);
            return null;
        }
    }
}
