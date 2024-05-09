package com.zbinyds.easyflv.util;

import lombok.SneakyThrows;
import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.FFmpegFrameRecorder;

import java.io.ByteArrayOutputStream;

/**
 * javacv 操作 ffmpeg 工具类
 *
 * @Author zbinyds
 * @Create 2024-05-08 15:47
 */
public class JavaCvUtil {
    @SneakyThrows
    public static FFmpegFrameGrabber createGrabber(String url) {
        FFmpegFrameGrabber grabber = new FFmpegFrameGrabber(url);
        grabber.setOption("rtsp_transport", "tcp");
        grabber.setOption("stimeout", "5000000");
        grabber.start();

        return grabber;
    }

    @SneakyThrows
    public static FFmpegFrameRecorder createRecorder(ByteArrayOutputStream stream, FFmpegFrameGrabber grabber) {
        FFmpegFrameRecorder recorder = new FFmpegFrameRecorder(stream, grabber.getImageWidth(), grabber.getImageHeight(),
                grabber.getAudioChannels());
        recorder.setInterleaved(true);
        recorder.setVideoOption("preset", "ultrafast");
        recorder.setVideoOption("tune", "zerolatency");
        recorder.setVideoOption("crf", "20");
        recorder.setFrameRate(grabber.getFrameRate());
        recorder.setSampleRate(grabber.getSampleRate());
        if (grabber.getAudioChannels() > 0) {
            recorder.setAudioChannels(grabber.getAudioChannels());
            recorder.setAudioBitrate(grabber.getAudioBitrate());
        }
        recorder.setFormat("flv");
        recorder.setVideoBitrate(grabber.getVideoBitrate());
        return recorder;
    }
}
