package com.hmall.cart.listener;

import com.hmall.cart.service.ICartService;
import com.hmall.common.utils.UserContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class ClearCartListener {

    private final ICartService cartService;

    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(name = "cart.clear.queue"),
            exchange = @Exchange(name = "trade.topic", type = ExchangeTypes.TOPIC),
            key = "order.create"
    ))
    public void clearCart(Map<String, Object> message) {
        log.info("清空购物车队列收到的消息：{}", message);
        Long userId = (Long) message.get("userId");
        UserContext.setUser(userId);
        List<Long> itemIds = (List<Long>) message.get("itemIds");
        cartService.removeByItemIds(itemIds);
    }

}
