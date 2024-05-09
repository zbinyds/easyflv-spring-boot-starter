package com.zbinyds.easyflv.service.impl;

import com.zbinyds.easyflv.service.AbstractConverter;
import com.zbinyds.easyflv.util.JavaCvUtil;
import org.bytedeco.ffmpeg.avcodec.AVPacket;
import org.bytedeco.ffmpeg.global.avcodec;
import org.bytedeco.ffmpeg.global.avutil;
import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.FFmpegFrameRecorder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.AsyncContext;
import java.io.ByteArrayOutputStream;

/**
 * rtsp流，视频格式为H264，音频格式为AAC
 *
 * @Author zbinyds
 * @Create 2024-05-08 14:25
 */
public class DefaultConverterThread extends AbstractConverter {
    private static final Logger log = LoggerFactory.getLogger(DefaultConverterThread.class);

    /**
     * 抓图器
     */
    private final FFmpegFrameGrabber grabber;

    /**
     * 保存转换好的流
     */
    private final ByteArrayOutputStream stream = new ByteArrayOutputStream();

    public DefaultConverterThread(String key, String url, AsyncContext context) {
        super(key, url, context);
        this.grabber = JavaCvUtil.createGrabber(url);
    }

    @Override
    public void run() {
        FFmpegFrameRecorder recorder = null;
        try {
            log.info("url:[{}] DefaultConverterThread start", super.getUrl());
            avutil.av_log_set_level(avutil.AV_LOG_ERROR);
            recorder = JavaCvUtil.createRecorder(stream, grabber);

            if (super.getHeaders() == null) {
                super.setHeaders(stream.toByteArray());
                stream.reset();
                writeResponse(super.getOuts(), super.getHeaders());
            }
            int nullNumber = 0;
            recorder.setAudioCodec(grabber.getAudioCodec());
            recorder.setVideoCodec(grabber.getVideoCodec());
            recorder.start(grabber.getFormatContext());

            while (isRunning) {
                AVPacket k = grabber.grabPacket();
                if (k != null) {
                    try {
                        recorder.recordPacket(k);
                    } catch (Exception ignored) {
                    }
                    if (stream.size() > 0) {
                        if (super.getOuts().isEmpty()) {
                            log.info("没有输出退出");
                            break;
                        }

                        byte[] b = stream.toByteArray();
                        stream.reset();
                        writeResponse(super.getOuts(), b);
                    }
                    avcodec.av_packet_unref(k);
                } else {
                    // 抓包失败次数达200次不等待默认退出
                    nullNumber++;
                    if (nullNumber > 200) {
                        log.info("抓包失败达上限, 当前{}次", nullNumber);
                        break;
                    }
                }
                // 此处根据实际带宽调整，如果读流器一直抓包失败，可以考虑加长等待时间
                Thread.sleep(10);
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        } finally {
            log.info("url:[{}] defaultConverter exit", super.getUrl());
            safeClose(this.grabber, recorder, this.stream, super.getOuts(), super.getKey());
        }
    }
}
