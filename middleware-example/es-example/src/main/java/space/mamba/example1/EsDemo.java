package space.mamba.example1;

import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.client.transport.TransportClient;
import space.mamba.util.JacksonUtil;

import java.io.IOException;
import java.util.Date;

import static org.elasticsearch.common.xcontent.XContentFactory.jsonBuilder;

/**
 * @author pankui
 * @date 2018/9/24
 * <pre>
 *
 * </pre>
 */
public class EsDemo {

    static TransportClient client = EsFactory.getTransportClient();

    public EsDemo() throws IOException {
    }

    public static void main(String[] args) {

        //创建索引
        IndexResponse indexResponse = index();
        System.out.println("创建索引" + JacksonUtil.toJSon(indexResponse));

        // 查询
        System.out.println(JacksonUtil.toJSon(get()));

    }

    public static GetResponse get() {
        GetResponse response = client.prepareGet("twitter", "tweet", "1").get();
        return response;
    }

    public static IndexResponse index() {

        try {
            IndexResponse response = client.prepareIndex("twitter", "tweet", "1").setSource(
                    jsonBuilder().startObject().field("user", "kimchy").field("postDate", new Date())
                            .field("message", "trying out Elasticsearch").endObject()).get();
            return response;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

}
