package com.zbinyds.easyflv.util;

import lombok.SneakyThrows;
import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.FFmpegFrameRecorder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;

/**
 * javacv 操作 ffmpeg 工具类
 *
 * @Author zbinyds
 * @Create 2024-05-08 15:47
 */
public class JavaCvUtil {
    private static final Logger log = LoggerFactory.getLogger(JavaCvUtil.class);

    /**
     * 创建抓图器并运行。
     *
     * @param url 抓取地址源
     * @return {@link FFmpegFrameGrabber 抓图器}
     */
    public static FFmpegFrameGrabber createGrabber(String url) {
        try {
            FFmpegFrameGrabber grabber = new FFmpegFrameGrabber(url);
            grabber.setTimeout(5000);
            grabber.setOption("rtsp_transport", "tcp");
            grabber.setOption("stimeout", "5000000");
            // 若有必要可以限制请求方式为POST，但对应的接口请求方式也必须是POST
//            grabber.setOption("method", "POST");
            grabber.start();
            return grabber;
        } catch (FFmpegFrameGrabber.Exception e) {
            log.error("运行抓图器FFmpegFrameGrabber失败, error: {}", e.getMessage());
            return null;
        }
    }

    /**
     * 创建取流器并运行。
     *
     * @param stream  待保存的流
     * @param grabber 抓图器
     * @return {@link FFmpegFrameRecorder 取流器}
     */
    @SneakyThrows
    public static FFmpegFrameRecorder createRecorder(ByteArrayOutputStream stream, FFmpegFrameGrabber grabber) {
        FFmpegFrameRecorder recorder = new FFmpegFrameRecorder(stream, grabber.getImageWidth(), grabber.getImageHeight(),
                grabber.getAudioChannels());
        recorder.setInterleaved(true);
        // 设置编码速度，以最快的速度编码
        recorder.setVideoOption("preset", "ultrafast");
        // 设置最小化延迟确保实时性
        recorder.setVideoOption("tune", "zerolatency");
        // 设置视频编码质量，值越低质量越高，0表示无损
        recorder.setVideoOption("crf", "20");
        recorder.setFrameRate(grabber.getFrameRate());
        recorder.setSampleRate(grabber.getSampleRate());
        if (grabber.getAudioChannels() > 0) {
            recorder.setAudioChannels(grabber.getAudioChannels());
            recorder.setAudioBitrate(grabber.getAudioBitrate());
        }
        // 指定输出格式为flv
        recorder.setFormat("flv");
        recorder.setVideoBitrate(grabber.getVideoBitrate());
        return recorder;
    }
}
