package com.hmall.api.client;

import com.hmall.api.dto.CartFormDTO;
import com.hmall.api.vo.CartVO;
import feign.Param;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.Collection;
import java.util.List;

@FeignClient("cart-service")
public interface CartClient {

    @DeleteMapping
    public void deleteCartItemByIds(@RequestParam("ids") Collection<Long> ids);

    @PostMapping
    public void addItem2Cart(@Valid @RequestBody CartFormDTO cartFormDTO);

    @DeleteMapping("{id}")
    public void deleteCartItem(@Param("购物车条目id")@PathVariable("id") Long id);

    @GetMapping
    public List<CartVO> queryMyCarts();
}
