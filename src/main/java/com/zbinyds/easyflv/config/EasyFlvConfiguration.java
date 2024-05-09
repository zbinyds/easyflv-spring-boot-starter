package com.zbinyds.easyflv.config;

import com.zbinyds.easyflv.service.helper.FlvHelper;
import com.zbinyds.easyflv.service.helper.impl.FlvHelperImpl;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;

/**
 * @Author zbinyds
 * @Create 2024-05-09 13:50
 */

@Configuration
public class EasyFlvConfiguration {

    @Bean
    @Order(1)
    @ConditionalOnMissingBean(FlvHelper.class)
    public FlvHelper flvHelper() {
        return new FlvHelperImpl();
    }

    // 后续可以拓展bean
}
