package com.zbinyds.easyflv.util;

import lombok.SneakyThrows;
import org.bytedeco.ffmpeg.global.avcodec;
import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.FFmpegFrameRecorder;

import java.io.ByteArrayOutputStream;

/**
 * javacv 操作 ffmpeg 工具类
 *
 * @Author zbinyds
 * @Create 2024-05-08 15:47
 */
public abstract class JavaCvUtil {

    /**
     * 创建抓图器并运行。
     *
     * @param url 抓取地址源
     * @return {@link FFmpegFrameGrabber 抓图器}
     */
    public static FFmpegFrameGrabber createGrabber(String url) {
        FFmpegFrameGrabber grabber = new FFmpegFrameGrabber(url);
        grabber.setTimeout(5000);
        grabber.setOption("rtsp_transport", "tcp");
        grabber.setOption("stimeout", "5000000");
        return grabber;
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
        // 设置交织模式，便于音视频同步播放
        recorder.setInterleaved(true);
        // 设置编码速度，以最快的速度编码
        recorder.setVideoOption("preset", "ultrafast");
        // 设置最小化延迟确保实时性
        recorder.setVideoOption("tune", "zerolatency");
        // 设置视频编码质量，值越低质量越高（0-51），0表示无损
        // 这里使用vbr模式，禁用固定比特率（videoBit设为0），通过crf设置视频质量
        recorder.setVideoOption("crf", "20");
        recorder.setVideoBitrate(0);

        // 设置视频帧率，一般是内部通过计算得到平均帧率
        recorder.setFrameRate(grabber.getFrameRate());
        // 设置音频采样率为44100hz（仅支持44100，22050，11025）
        recorder.setSampleRate(44100);
        if (grabber.getAudioChannels() > 0) {
            // 如果原视频流有音频输出
            // 设置音频通道为grabber音频通道（0-无音频 1-单声道 2-立体声...）
            recorder.setAudioChannels(grabber.getAudioChannels());
            // 设置音频比特率（64kbps-192kbps），这里强制设置为128kbps
            recorder.setAudioBitrate(128000);
        }
        // 指定输出格式为flv
        recorder.setFormat("flv");

        // 设置视频解码器为H264、音频编解码器为AAC（这里可选MP3）
        recorder.setAudioCodec(avcodec.AV_CODEC_ID_AAC);
        recorder.setVideoCodec(avcodec.AV_CODEC_ID_H264);
        return recorder;
    }
}
