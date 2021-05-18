package space.mamba.mamba;

import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.TransportAddress;
import org.elasticsearch.transport.client.PreBuiltTransportClient;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * @author pankui
 * @date 2018/9/24
 * <pre>
 *
 * </pre>
 */
public class EsFactory {


    public static TransportClient getTransportClient() {
        TransportClient client = null;
        try {
            Settings settings = Settings.builder()
                    .put("cluster.name", "elasticsearch").build();

            client = new PreBuiltTransportClient(settings)
                    .addTransportAddress(new TransportAddress(InetAddress.getByName("127.0.0.1"), 9300));
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }

        return client;

    }

}


