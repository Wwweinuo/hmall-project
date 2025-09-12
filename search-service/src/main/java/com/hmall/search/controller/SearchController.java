package com.hmall.search.controller;



import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
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
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

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
    public PageDTO<ItemDTO> search(ItemPageQuery query) throws IOException {
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
        log.info("query: " + query);
        // 1.配置查询参数
        BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();
        // name模糊查询
        if (StrUtil.isNotBlank(query.getKey())) {
            boolQuery.must(QueryBuilders.matchQuery("name", query.getKey()));
        }
        // brand、category、status精确匹配
        if (StrUtil.isNotBlank(query.getBrand())) {
            boolQuery.filter(QueryBuilders.termQuery("brand.keyword", query.getBrand()));
        }
        if (StrUtil.isNotBlank(query.getCategory())) {
            boolQuery.filter(QueryBuilders.termQuery("category.keyword", query.getCategory()));
        }
//        boolQuery.must(QueryBuilders.termQuery("isAD", false));
        // price范围查询
        if (query.getMaxPrice() != null) {
            boolQuery.filter(QueryBuilders.rangeQuery("price")
                    .gte(query.getMinPrice() == null ? 0 : query.getMinPrice())
                    .lte(query.getMaxPrice()));
        }
        // 2.构建分页+排序
        SearchSourceBuilder sourceBuilder = SearchSourceBuilder.searchSource()
                .query(boolQuery)
                .from(query.getPageNo() - 1)
                .size(query.getPageSize())
                .sort("updateTime", SortOrder.DESC);
        // 3.构建请求
        SearchRequest request = new SearchRequest("items").source(sourceBuilder);
        // 4.发送请求
        SearchResponse response =client.search(request, RequestOptions.DEFAULT);
        log.info("response: " + response);
        // 5.解析结果
        List<ItemDTO> records = Arrays.stream(response.getHits().getHits())
                .map(hit -> {
                    return JSONUtil.toBean(hit.getSourceAsString(), ItemDTO.class);
                })
                .collect(Collectors.toList());
        long total = response.getHits().getTotalHits().value;
        // 6.封装分页对象
        return new PageDTO<>(total, total / query.getPageSize(), records);
    }

}
