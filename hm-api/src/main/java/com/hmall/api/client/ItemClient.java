package com.hmall.api.client;

import com.hmall.api.client.fallback.ItemClientFallback;
import com.hmall.api.dto.ItemDTO;
import com.hmall.api.dto.OrderDetailDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;
import java.util.Collection;
import java.util.List;


@FeignClient(value = "item-service", path = "/items", fallbackFactory = ItemClientFallback.class)
public interface ItemClient {

    @GetMapping
    List<ItemDTO> queryItemByIds(@RequestParam("ids") Collection<Long> ids);

    @PutMapping("/stock/deduct")
    public void deductStock(@RequestBody List<OrderDetailDTO> items);


    @PutMapping("/stock/restore")
    public void restoreStock(@RequestBody List<OrderDetailDTO> items);
}