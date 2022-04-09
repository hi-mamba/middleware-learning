package space.mamba;

import com.googlecode.jsonrpc4j.JsonRpcService;

/**
 * @author meta a
 * @date 2022/4/9
 * <pre>
 *
 *
 * curl --location --request POST 'http://127.0.0.1:8080/rpc/api' \
 * --header 'Content-Type;' \
 * --data-raw '{
 *     "method":"hello",
 *     "params":["a"],
 *     "jsonrpc":"2.0",
 *     "id":0
 * }'
 *
 * </pre>
 */
@JsonRpcService("rpc/api")
public interface ProductJsonRpcService {

    /**
     *
     * @param name
     * @return
     */
    public String hello(String name);
}
