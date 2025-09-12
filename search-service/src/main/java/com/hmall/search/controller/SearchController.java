package com.hmall.search.controller;



import cn.hutool.core.bean.BeanUtil;
import cn.hutool.json.JSONUtil;
import com.hmall.common.domain.PageDTO;
import com.hmall.search.domain.dto.ItemDTO;
import com.hmall.search.domain.po.Item;
import com.hmall.search.domain.query.ItemPageQuery;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpHost;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@Api(tags = "搜索相关接口")
@RestController
@RequestMapping("/search")
@RequiredArgsConstructor
@Slf4j
public class SearchController {

    private final RestHighLevelClient client = new RestHighLevelClient(RestClient.builder(
            new HttpHost("192.168.213.129", 9200))
    );

    @ApiOperation("根据id查询商品")
    @GetMapping("/{id}")
    public Item queryItemById(@PathVariable("id") Long id) throws IOException {
        // 创建request
        SearchRequest request = new SearchRequest("items");
        // 准备请求参数
        request.source().query(QueryBuilders.termQuery("id", id.toString()));
        // 发送请求
        SearchResponse response = client.search(request, RequestOptions.DEFAULT);
        System.out.println("response: " + response);
        // 解析聚合结果
        SearchHits searchHits = response.getHits();
        SearchHit[] hits = searchHits.getHits();
        String json = hits[0].getSourceAsString();
        log.info("json: " + json);
        ItemDTO itemDTO = JSONUtil.toBean(json, ItemDTO.class);
        return BeanUtil.copyProperties(itemDTO, Item.class);
    }

    @ApiOperation("搜索商品")
    @GetMapping("/list")
    public PageDTO<ItemDTO> search(ItemPageQuery query) {
//        // 分页查询
//        Page<Item> result = itemService.lambdaQuery()
//                .like(StrUtil.isNotBlank(query.getKey()), Item::getName, query.getKey())
//                .eq(StrUtil.isNotBlank(query.getBrand()), Item::getBrand, query.getBrand())
//                .eq(StrUtil.isNotBlank(query.getCategory()), Item::getCategory, query.getCategory())
//                .eq(Item::getStatus, 1)
//                .between(query.getMaxPrice() != null, Item::getPrice, query.getMinPrice(), query.getMaxPrice())
//                .page(query.toMpPage("update_time", false));
//        // 封装并返回
//        return PageDTO.of(result, ItemDTO.class);
        return null;
    }

}
