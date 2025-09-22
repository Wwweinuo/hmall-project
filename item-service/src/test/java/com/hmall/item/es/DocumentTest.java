package com.hmall.item.es;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hmall.item.domain.po.Item;
import com.hmall.item.domain.po.ItemDoc;
import com.hmall.item.service.IItemService;
import org.apache.http.HttpHost;
import org.apache.lucene.index.Term;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.CreateIndexRequest;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.Aggregation;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.endpoint.event.RefreshEventListener;

import java.io.IOException;
import java.util.List;

@SpringBootTest(properties = "spring.profiles.active=local")
public class DocumentTest {

    private RestHighLevelClient client;
    @Autowired
    private IItemService itemService;
    @Autowired
    private RefreshEventListener refreshEventListener;

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
    void testAgg() throws IOException {
        // 创建request
        SearchRequest request = new SearchRequest("items");
        // 准备请求参数
        request.source().query(QueryBuilders.boolQuery()
                .filter(QueryBuilders
                        .termQuery("category.keyword", "手机")))
                .size(0);
        // 聚合函数
        request.source().aggregation(
                AggregationBuilders
                        .terms("brand_agg")
                        .field("brand.keyword")
                        .size(5)
                );
        // 发送请求
        SearchResponse response = client.search(request, RequestOptions.DEFAULT);
        System.out.println("response: " + response);
        // 解析聚合结果
        Aggregations aggregations = response.getAggregations();
        // 获取品牌聚合
        Terms brandTerms = aggregations.get("brand_agg");
        // 获取聚合中的桶
        List<? extends Terms.Bucket> buckets = brandTerms.getBuckets();
        for (Terms.Bucket bucket : buckets) {
            // 获取桶中的key
            String brand = bucket.getKeyAsString();
            System.out.println("品牌：" + brand);
            long docCount = bucket.getDocCount();
            System.out.println("品牌文档数：" + docCount);
        }

    }


    @Test
    void testMatchAll() throws IOException {
        // 1.创建Request
        SearchRequest request = new SearchRequest("items");
        // 2.组织请求参数
        SearchSourceBuilder query = request.source().query(QueryBuilders.matchAllQuery());
        // 3.发送请求
        SearchResponse response = client.search(request, RequestOptions.DEFAULT);
        // 4.解析响应
        handlerResponse(response);
    }

    private void handlerResponse(SearchResponse response) {
        SearchHits searchHits = response.getHits();
        // 1.获取总条数
        long total = searchHits.getTotalHits().value;
        System.out.println("共搜索到" + total + "条数据");
        // 2.遍历结果数组
        SearchHit[] hits = searchHits.getHits();
        for (SearchHit hit : hits) {
            // 3.得到_source，也就是原始json文档
            String source = hit.getSourceAsString();
            // 4.反序列化并打印
            ItemDoc item = JSONUtil.toBean(source, ItemDoc.class);
            System.out.println(item);
        }
    }



    @Test
    void testBulk() throws IOException {
        int pageNo = 1;
        int pageSize = 500;
        while (true) {
            // 分页查询商品
            Page<Item> page = itemService.lambdaQuery().eq(Item::getStatus, 1).page(new Page<Item>(pageNo, pageSize));
            List<Item> items = page.getRecords();
            if (items.isEmpty()) {
                return ;
            }
            // 创建索引请求
            BulkRequest request = new BulkRequest("items");
            for (Item item : items) {
                // 准备请求参数
                ItemDoc itemDoc = BeanUtil.copyProperties(item, ItemDoc.class);
                request.add(new IndexRequest("items").source(JSONUtil.toJsonStr(itemDoc), XContentType.JSON));
            }
            // 发送请求
            client.bulk(request, RequestOptions.DEFAULT);
            pageNo++;
        }

    }

    @Test
    void testIndexDoc() throws IOException {
        // 准备文档数据
        Item item = itemService.getById(584387);
        ItemDoc itemDoc = BeanUtil.copyProperties(item, ItemDoc.class);
        System.out.println(itemDoc.toString());


        IndexRequest request = new IndexRequest("items").id(itemDoc.getId());
        request.source(JSONUtil.toJsonPrettyStr(itemDoc), XContentType.JSON);
        client.index(request, RequestOptions.DEFAULT);
    }


    @AfterEach
    void tearDown() throws IOException {
        this.client.close();
    }



}