package com.hmall.api.client;

import com.hmall.api.dto.OrderFormDTO;
import com.hmall.api.vo.OrderVO;
import feign.Param;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

@FeignClient(name = "trade-service")
public interface TradeClient {

    @PutMapping("/{orderId}")
    public void markOrderPaySuccess(@PathVariable("orderId") Long orderId);

    @GetMapping("{id}")
    public OrderVO queryOrderById(@Param("订单id")@PathVariable("id") Long orderId);

    @PostMapping
    public Long createOrder(@RequestBody OrderFormDTO orderFormDTO);
}
