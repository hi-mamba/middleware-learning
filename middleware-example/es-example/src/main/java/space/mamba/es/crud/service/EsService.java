package space.mamba.es.crud.service;

import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.action.DocWriteResponse;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.action.admin.indices.settings.put.UpdateSettingsRequest;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.support.master.AcknowledgedResponse;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.CreateIndexRequest;
import org.elasticsearch.client.indices.CreateIndexResponse;
import org.elasticsearch.client.indices.GetIndexRequest;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentType;
import org.springframework.stereotype.Service;
import space.mamba.es.crud.dao.ESMapper;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;


/**
 * @author pankui
 * @date 2021/7/10 
 * <pre>
 *
 * </pre>  
 */
@Service
@Slf4j
public class EsService {

    @Resource
    private ESMapper esMapper;

    public void customSql() {
        Map<String, String> map = new HashMap<>();
        map.put("sql", "select * from user");
        log.info("result={}", esMapper.selectMap(map));
    }

    @Resource
    private RestHighLevelClient restHighLevelClient;


    /**
     * 创建索引
     *
     * @param index 索引名称
     * @param routing routing
     * @param json 索引的结构
     * @param id
     */
    public boolean createIndex(String index, String routing, String json, String id) {

        log.info("createIndex begin,index = {},type = {},id = {},json = {}", index, routing, id, json);
        CreateIndexRequest request = new CreateIndexRequest(index);
        request.source(json, XContentType.JSON);
        //创建索引操作客户端,创建响应对象
        CreateIndexResponse createIndexResponse;
        try {
            createIndexResponse = restHighLevelClient.indices().create(request, RequestOptions.DEFAULT);
            //得到响应结果
            boolean shardsAcknowledged = createIndexResponse.isShardsAcknowledged();
            if (shardsAcknowledged) {
                log.info("createIndex ok,index = {},routing = {},id = {},json = {}", index, routing, id, json);
                return true;
            }
        } catch (IOException e) {
            log.error("创建索引失败", e);
        }
        log.error("createIndex failed,index = {},routing = {},id = {},json = {},}", index, routing, id, json);
        return false;
    }

    /**
     * inputJsonMap 格式
     * key 是 主键，数组是存入数据g
     * */
    public boolean bulkCreateIndex(String index, String routing, Map<String, Object[]> inputJsonMap) {

        BulkRequest bulkRequest = new BulkRequest();
        bulkRequest.timeout(TimeValue.timeValueSeconds(10));
        bulkRequest.routing(routing);
        //批处理请求
        inputJsonMap.forEach((key, value) -> {
            IndexRequest indexRequest = new IndexRequest(index).id(key).source(value).routing(routing);
            bulkRequest.add(indexRequest);
        });
        BulkResponse bulkResponse = null;
        try {
            log.info("bulkRequest ={}", bulkRequest);
            bulkResponse = restHighLevelClient.bulk(bulkRequest, RequestOptions.DEFAULT);
        } catch (IOException e) {
            log.error("bulkCreateIndex error,index={},data={}", index, inputJsonMap, e);
        }
        if (bulkResponse == null) {
            log.error("bulkCreateIndex has failures,index = {},routing = {}", index, routing);
            return false;
        }
        // 是否失败，返回 false 代表 成功！
        if (bulkResponse.hasFailures()) {
            log.error("bulkCreateIndex has failures,index = {},type = {},error = {}", index, routing,
                    bulkResponse.buildFailureMessage());
            return false;
        }
        log.info("bulkCreateIndex ok ,index = {},type = {},inputJsonMap.size = {}", index, routing,
                inputJsonMap.size());
        return true;
    }


    public boolean bulkUpdateIndex(String index, String routing, Map<String, Object[]> inputJsonMap) {
        BulkRequest bulkRequest = new BulkRequest();
        bulkRequest.timeout(TimeValue.timeValueSeconds(10));
        bulkRequest.routing(routing);
        BulkResponse bulkResponse = null;
        try {
            //批处理请求
            inputJsonMap.forEach((key, value) -> {
                IndexRequest indexRequest = new IndexRequest(index).id(key).source(value);
                UpdateRequest updateRequest = new UpdateRequest().index(index).id(key).routing(index).doc(value)
                        .routing(index).upsert(indexRequest);
                bulkRequest.add(updateRequest);
            });
            log.info("bulkRequest ={}", bulkRequest);
            bulkResponse = restHighLevelClient.bulk(bulkRequest, RequestOptions.DEFAULT);
        } catch (IOException e) {
            log.error("bulkUpdateIndex error,index={},data={}", index, inputJsonMap, e);
        }
        if (bulkResponse == null) {
            log.error("bulkUpdateIndex has failures,index = {},routing = {}", index, routing);
            return false;
        }
        // 是否失败，返回 false 代表 成功！
        if (bulkResponse.hasFailures()) {
            log.error("bulkUpdateIndex has failures,index = {},tyroutingpe = {},error = {}", index, routing,
                    bulkResponse.buildFailureMessage());
            return false;
        }
        log.info("bulkUpdateIndex ok ,index = {},routing = {},inputJsonMap.size = {}", index, routing,
                inputJsonMap.size());
        return true;
    }


    public boolean bulkDeleteIndex(String index, String type, Map<String, Object[]> inputJsonMap) {
        BulkRequest bulkRequest = new BulkRequest();
        bulkRequest.timeout(TimeValue.timeValueSeconds(10));
        bulkRequest.routing(index);
        BulkResponse bulkResponse = null;
        try {
            //批处理请求
            inputJsonMap.forEach((key, value) -> bulkRequest.add(new DeleteRequest(index, key)));
            bulkResponse = restHighLevelClient.bulk(bulkRequest, RequestOptions.DEFAULT);
        } catch (IOException e) {
            log.error("bulkDeleteIndex error,index={},data={}", index, inputJsonMap, e);
        }
        if (bulkResponse == null) {
            log.error("bulkDeleteIndex has failures,index = {},type = {}", index, type);
            return false;
        }
        // 是否失败，返回 false 代表 成功！
        if (bulkResponse.hasFailures()) {
            log.error("bulkDeleteIndex has failures,index = {},type = {},error = {}", index, type,
                    bulkResponse.buildFailureMessage());
            return false;
        }
        log.info("bulkDeleteIndex ok ,index = {},type = {},inputJsonMap.size = {}", index, type, inputJsonMap.size());
        return true;
    }


    /**
     *  判断索引是否存在
     *
     *  @param indexName 索引名称
     *  @return 是否存在 是:true 否:false
     *     */

    public boolean existsIndex(String indexName) {
        GetIndexRequest request = new GetIndexRequest(indexName);
        try {
            return restHighLevelClient.indices().exists(request, RequestOptions.DEFAULT);
        } catch (IOException e) {
            log.error("查询索引是否存在异常={}", indexName, e);
        }
        return false;
    }

    /**
     * 更新索引
     *
     * @param index
     * @param type
     * @param json
     * @param id
     */

    public boolean updateIndex(String index, String type, String json, String id, String routing) {
        boolean flag = false;
        try {
            log.info("updateIndex begin,index = {},type = {},id = {},json = {}", index, type, id, json);
            UpdateSettingsRequest request = new UpdateSettingsRequest(index);
            //restHighLevelClient.indices().updateAliases();
            //if (RestStatus.OK != updateResponse.status()) {
            //    log.error("updateIndex failed,index = {},type = {},id = {},json = {}", index, type, id, json);
            //} else {
            //    flag = true;
            //    log.info("updateIndex ok,index = {},type = {},id = {},json = {}", index, type, id, json);
            //}

        } catch (Exception e) {
            log.error("updateIndex error,index = {},type = {},id = {},error = {}", index, type, id, e.getMessage());
        }
        return flag;
    }


    /**
     * 删除索引
     *
     * @param index
     * @param type
     */

    public boolean deleteIndex(String index, String type, String routing) {
        DeleteIndexRequest request = new DeleteIndexRequest(index);
        // 删除
        try {
            AcknowledgedResponse delete = restHighLevelClient.indices().delete(request, RequestOptions.DEFAULT);
            return delete.isAcknowledged();
        } catch (IOException e) {
            log.error("删除索引异常={}", index, e);
        }
        return false;
    }

    /**
     * 设置mapping 同时创建新index
     *
     * @param index
     * @param mappingBuilder
     */

    public boolean putMapping(String index, String type, XContentBuilder mappingBuilder) {
        try {
            if (mappingBuilder != null) {
                String jsonSource = mappingBuilder.toString();
                log.info("putMapping,index = {},type = {},mapping = {}", index, type, jsonSource);
                // CreateIndexResponse response = restHighLevelClient.indices()
                //         .addMapping(type, mappingBuilder).get();
                // return response.isAcknowledged();
            }
        } catch (Exception e) {
            log.error("putMapping error ", e);
            return false;
        }
        return true;
    }

    /**
     * 为已存在的 index 设置mapping
     *
     * @param index
     * @param mappingBuilder
     */

    public boolean putMapping(String index, String type, String settingSource, XContentBuilder mappingBuilder) {
        try {
            if (mappingBuilder != null) {
                String jsonSource = mappingBuilder.toString();
                log.info("putMapping,index = {},mapping = {}", index, jsonSource);
                // CreateIndexResponse response = CLIENT.admin().indices().prepareCreate(index).setSettings
                //(settingSource)
                //         .addMapping(type, mappingBuilder).get();
                // return response.isAcknowledged();
            }
        } catch (Exception e) {
            log.error("putMapping error ", e);
            return false;
        }
        return true;
    }

    /**
     * 删除索引及其全部文档
     *
     * @param indexName 索引名称
     * @return 是否删除 是:true 否:false
     */

    public boolean deleteIndexByName(String indexName) {
        boolean flag = false;
        try {
            // DeleteIndexResponse dResponse = CLIENT.admin().indices().prepareDelete(indexName).execute().actionGet();
            // if (dResponse.isAcknowledged()) {
            //     log.info("deleteIndexByName ok,indexName = {}", indexName);
            //     flag = true;
            // } else {
            //     log.error("deleteIndexByName failed,indexName = {},reason = {}", indexName, dResponse.toString());
            // }
        } catch (Exception e) {
            log.error("deleteIndexByName error,indexName = {},error = {}", indexName, e.getMessage());
        }
        return flag;
    }

    /**
     * 添加文档
     * */

    public boolean addDocument(String indexName, String jsonData, String id) {
        // 创建请求
        IndexRequest request = new IndexRequest(indexName);
        // 规则 put /indexName/_doc/1
        request.id(id);
        request.routing(indexName);
        request.timeout(TimeValue.timeValueSeconds(1));
        // 将我们的数据放入请求 json
        request.source(jsonData, XContentType.JSON);
        // 客户端发送请求 , 获取响应的结果
        try {
            IndexResponse indexResponse = restHighLevelClient.index(request, RequestOptions.DEFAULT);
            log.info("add document result ={}", indexResponse);
            return indexResponse.getResult().equals(DocWriteResponse.Result.CREATED);
        } catch (IOException e) {
            log.error("添加document异常,indexName={},data={}", indexName, jsonData, e);
        }
        return false;
    }

    /**
     * 更新文档
     * upsert--id不存在时就插入
     * */

    public boolean updateDocument(String indexName, String routing, String jsonData, String id) {
        log.info("update document jsonData={},id={},routing={},index={}", jsonData, id, routing, indexName);
        UpdateRequest updateRequest = new UpdateRequest(indexName, id);
        updateRequest.upsert(jsonData, XContentType.JSON);
        updateRequest.routing(routing);
        try {
            UpdateResponse updateResponse = restHighLevelClient.update(updateRequest, RequestOptions.DEFAULT);
            log.info("update document result ={}", updateResponse);
            if (updateResponse.getResult() == DocWriteResponse.Result.CREATED) {
                // 首次创建或upsert
                log.info("首次创建或upsert ={},jsonData={}", indexName, jsonData);
                return true;
            } else if (updateResponse.getResult() == DocWriteResponse.Result.UPDATED) {
                // 文档被更新
                log.info("文档被更新 ={},jsonData={}", indexName, jsonData);
                return true;
            } else if (updateResponse.getResult() == DocWriteResponse.Result.NOOP) {
                // 未对已有文档造成影响
                log.info("未对已有文档造成影响 ={},jsonData={}", indexName, jsonData);
                return true;
            }
            log.info("未知更新状态 ={}", updateResponse.getResult());
        } catch (IOException e) {
            log.error("update document异常,indexName={},data={}", indexName, jsonData, e);
        }
        return false;
    }


    /**
     * 删除文档
     * */

    public boolean deleteDocument(String indexName, String routing, String id) {
        DeleteRequest request = new DeleteRequest(indexName, id);
        request.routing(routing);
        try {
            DeleteResponse deleteResponse = restHighLevelClient.delete(request, RequestOptions.DEFAULT);
            log.info("delete document result ={}", deleteResponse);
            return deleteResponse.getResult().equals(DocWriteResponse.Result.DELETED);
        } catch (IOException e) {
            log.error("delete document异常,indexName={},data={}", indexName, routing, e);
        }
        return false;
    }

    /**
     * 获取文档
     * */

    public String getDocument(String indexName, String routing, String id) {
        GetRequest getRequest = new GetRequest(indexName, id);
        getRequest.routing(indexName);
        try {
            GetResponse getResponse = restHighLevelClient.get(getRequest, RequestOptions.DEFAULT);
            return getResponse.getSourceAsString();
        } catch (IOException e) {
            log.error("获取 document异常,indexName={}", indexName, e);
        }
        return null;
    }

    /**
     * 是否存在文档
     * */

    public boolean existsDocument(String indexName, String routing, String id) {
        GetRequest getRequest = new GetRequest(indexName, id);
        getRequest.routing(routing);
        try {
            return restHighLevelClient.exists(getRequest, RequestOptions.DEFAULT);
        } catch (IOException e) {
            log.error("是否存在文档 document异常,indexName={}", indexName, e);
        }
        return false;
    }
}
    

