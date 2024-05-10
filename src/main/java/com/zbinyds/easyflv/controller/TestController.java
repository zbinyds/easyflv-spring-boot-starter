package com.zbinyds.easyflv.controller;

import com.zbinyds.easyflv.service.helper.FlvHelper;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @Author zbinyds
 * @Create 2024-05-08 14:41
 */

@CrossOrigin
@RestController
@RequestMapping
@RequiredArgsConstructor
public class TestController {
    private final FlvHelper flvHelper;

    @GetMapping("/stream_{channel}.flv")
    public void flv(@PathVariable String channel, @RequestParam Integer type,
                    HttpServletRequest request, HttpServletResponse response) {
        String url;
        if (type == 1) {
            url = "rtsp://admin:Aa123456@223.112.40.138:25082/Streaming/Channels/" + channel;
        } else if (type == 2) {
            url = "rtsp://admin:Aa123456@223.112.40.138:25182/Streaming/Channels/" + channel;
        } else {
            url = "rtsp://admin:Aa123456@223.112.40.138:16896/Streaming/Channels/" + channel;
        }
        flvHelper.open(url, request, response);
    }
}
