package com.hmall.search.es;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.json.JSONUtil;
import com.hmall.search.domain.dto.ItemDTO;
import com.hmall.search.domain.po.Item;
import org.apache.http.HttpHost;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.web.bind.annotation.PathVariable;

import java.io.IOException;

@SpringBootTest
public class DucumentTest {

    private RestHighLevelClient client;

    @BeforeEach
    void setUp() {
        this.client = new RestHighLevelClient(RestClient.builder(
                HttpHost.create("http://192.168.213.129:9200")
        ));
    }

    @Test
    void testConnect() {
        System.out.println(client);
    }

    @Test
    void test2() throws IOException {
        // 创建request
        SearchRequest request = new SearchRequest("items");
        // 准备请求参数
        request.source().query(QueryBuilders.termQuery("id", "11892989666"));

        // 发送请求
        SearchResponse response = client.search(request, RequestOptions.DEFAULT);
        System.out.println("response: " + response);
        // 解析聚合结果
        SearchHits searchHits = response.getHits();
        SearchHit[] hits = searchHits.getHits();
        String json = hits[0].getSourceAsString();
        System.out.println("json: " + json);
        ItemDTO itemDTO = JSONUtil.toBean(json, ItemDTO.class);
        System.out.println("itemDTO: " + itemDTO);
    }
}
