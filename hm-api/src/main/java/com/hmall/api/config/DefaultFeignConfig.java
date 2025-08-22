package com.hmall.api.config;

import com.hmall.common.utils.UserContext;
import feign.Logger;
import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DefaultFeignConfig {

    @Bean
    Logger.Level feignLoggerLevel() {
        return Logger.Level.FULL;
    }

    @Bean
    public RequestInterceptor userInfoRequestInterceptor() {
        return new RequestInterceptor() {
            @Override
            public void apply(RequestTemplate requestTemplate) {
                // 判断用户id是否为空
                Long userId = UserContext.getUser();
                if (userId == null) {
                    // 为空则直接放行
                    return;
                }
                // 不为空则保存到请求头中
                requestTemplate.header("user-info", String.valueOf(userId));
            }
        };
    }

}
