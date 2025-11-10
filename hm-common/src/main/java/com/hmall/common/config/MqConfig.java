package com.hmall.common.config;

import cn.hutool.core.util.ObjectUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hmall.common.utils.UserContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConversionException;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnClass(RabbitTemplate.class)
@Slf4j
public class MqConfig {

    @Bean
//    @ConditionalOnBean(ObjectMapper.class)
    public MessageConverter messageConverter(){
        // 1.定义消息转换器
        Jackson2JsonMessageConverter converter = new Jackson2JsonMessageConverter() {
            // 从消息中获取用户信息
            @Override
            public Object fromMessage(Message message) throws MessageConversionException {
                Long userId = message.getMessageProperties().getHeader("user-info");
                log.info("从AMQP的消息中获取用户信息：" + userId);
                if (ObjectUtil.isNotNull(userId)) {
                    UserContext.setUser(userId);
                }
                return super.fromMessage(message);
            }
        };
        // 2.配置自动创建消息id，用于识别不同消息
        converter.setCreateMessageIds(true);
        return converter;
    }

    // 关键改造：在消息发送前写入用户信息到 Header
    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        // 添加消息发送前处理器
        rabbitTemplate.setBeforePublishPostProcessors(message -> {
            Long userId = UserContext.getUser();   // 从 ThreadLocal 取用户
            log.info("添加用户信息到消息中：" + userId);
            if (userId != null) {
                message.getMessageProperties().setHeader("user-info", userId);
            }
            return message;
        });
        // 添加消息发送后处理器
        rabbitTemplate.setAfterReceivePostProcessors(message -> {
            if (message != null) {
                MessageProperties properties = message.getMessageProperties();
                if (properties != null) {
                    Long userId = properties.getHeader("user-info");
                    log.info("从消息中获取用户信息：" + userId);
                    if (userId != null) {
                        UserContext.setUser(userId);
                    }
                }
            }
            return message;
        });
        rabbitTemplate.setMessageConverter(messageConverter());
        return rabbitTemplate;
    }


}
