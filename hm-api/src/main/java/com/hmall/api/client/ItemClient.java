package com.hmall.api.client;

import com.hmall.api.dto.ItemDTO;
import com.hmall.api.dto.OrderDetailDTO;
import io.swagger.annotations.ApiOperation;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;
import java.util.List;

@FeignClient("item-service")
public interface ItemClient {

    @GetMapping("/items")
    List<ItemDTO> queryItemByIds(@RequestParam("ids") Collection<Long> ids);

    @PutMapping("/stock/deduct")
    public void deductStock(@RequestBody List<OrderDetailDTO> items);

    @GetMapping("{id}")
    public ItemDTO queryItemById(@PathVariable("id") Long id);

    @PostMapping
    public void saveItem(@RequestBody ItemDTO item);

    @PutMapping("/status/{id}/{status}")
    public void updateItemStatus(@PathVariable("id") Long id, @PathVariable("status") Integer status);

    @PutMapping
    public void updateItem(@RequestBody ItemDTO item);

    @DeleteMapping("{id}")
    public void deleteItemById(@PathVariable("id") Long id);
}