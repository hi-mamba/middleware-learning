package space.mamba.util.http;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.Consts;
import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.config.ConnectionConfig;
import org.apache.http.config.MessageConstraints;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.config.SocketConfig;
import org.apache.http.conn.ConnectionPoolTimeoutException;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLContexts;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import space.mamba.util.JacksonUtil;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.IOException;
import java.net.SocketTimeoutException;
import java.nio.charset.CodingErrorAction;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Description
 * <p>
 * </p>
 * DATE 2017/9/19.
 *
 * @author pankui.
 */
@Slf4j
public class HttpClientUtil {


    private final static Logger logger = LoggerFactory.getLogger(HttpClientUtil.class);
    private final static int SOCKET_TIMEOUT = 50000;
    private final static int CONNECT_TIMEOUT = 30000;
    private static PoolingHttpClientConnectionManager connManager = null;
    private static CloseableHttpClient httpClient = null;
    private static BasicCookieStore cookieStore = new BasicCookieStore();

    private static String CONTENT_TYPE = "application/json";

    //同步
    static {
        try {
            // 相信自己的CA和所有自签名的证书
            SSLContext sslContext = SSLContexts.custom().useTLS().build();
            sslContext.init(null,
                    new TrustManager[]{new X509TrustManager() {

                        @Override
                        public X509Certificate[] getAcceptedIssuers() {
                            return null;
                        }

                        @Override
                        public void checkClientTrusted(
                                X509Certificate[] certs, String authType) {
                        }

                        @Override
                        public void checkServerTrusted(
                                X509Certificate[] certs, String authType) {
                        }
                    }}, null);
            Registry<ConnectionSocketFactory> socketFactoryRegistry = RegistryBuilder.<ConnectionSocketFactory>create()
                    .register("http", PlainConnectionSocketFactory.INSTANCE)
                    .register("https", new SSLConnectionSocketFactory(sslContext))
                    .build();
            /*   设置连接池  */

            connManager = new PoolingHttpClientConnectionManager(socketFactoryRegistry);
            // Create socket configuration
            SocketConfig socketConfig = SocketConfig.custom().setTcpNoDelay(true).build();
            connManager.setDefaultSocketConfig(socketConfig);
            // Create message constraints
            MessageConstraints messageConstraints = MessageConstraints.custom()
                    .setMaxHeaderCount(200)
                    .setMaxLineLength(2000)
                    .build();
            // Create connection configuration
            ConnectionConfig connectionConfig = ConnectionConfig.custom()
                    .setMalformedInputAction(CodingErrorAction.IGNORE)
                    .setUnmappableInputAction(CodingErrorAction.IGNORE)
                    .setCharset(Consts.UTF_8)
                    .setMessageConstraints(messageConstraints)
                    .build();
            connManager.setDefaultConnectionConfig(connectionConfig);
            /*设置连接池大小*/
            connManager.setMaxTotal(200);
            /*设置每个路由上的默认连接个数*/
            connManager.setDefaultMaxPerRoute(20);

            httpClient = HttpClients.custom()
                    .setDefaultCookieStore(cookieStore)
                    .setConnectionManager(connManager)
                    .build();

        } catch (KeyManagementException e) {
            logger.error("KeyManagementException", e);
        } catch (NoSuchAlgorithmException e) {
            logger.error("NoSuchAlgorithmException", e);
        }
    }

    public static String doPost(String url, List<NameValuePair> params, Map<String, String> headers) {
        return doPost(url, params, headers, null, SOCKET_TIMEOUT, CONNECT_TIMEOUT);
    }

    /**
     * HTTP Post 获取内容
     *
     * @param url     请求的url地址 ?之前的地址
     * @param params  请求的参数
     * @param charset 编码格式
     * @return 页面内容
     */
    public static String doPost(String url, List<NameValuePair> params, Map<String, String> headers,
                                String charset, int socketTimeout, int connectTimeout) {
        if (StringUtils.isBlank(url)) {
            return null;
        }
        long start = System.currentTimeMillis();
        String responseContent = null;
        HttpPost httpPost = new HttpPost(url);
        try {
            RequestConfig requestConfig = RequestConfig.custom()
                    .setSocketTimeout(socketTimeout)
                    .setConnectTimeout(connectTimeout)
                    .setConnectionRequestTimeout(connectTimeout).build();
            StringEntity stringEntity = null;

            if (params != null && !params.isEmpty()) {
                Map<String, Object> param = new HashMap<>();
                // 如果传递json，则需要设置header
                if (headers != null && headers.containsValue(CONTENT_TYPE)) {
                    for (NameValuePair entry : params) {
                        Object value = entry.getValue();
                        param.put(entry.getName(), value);
                    }
                    stringEntity = new StringEntity(JacksonUtil.toJSon(param), Consts.UTF_8);
                    stringEntity.setContentType(new BasicHeader(HTTP.CONTENT_TYPE, CONTENT_TYPE));
                    httpPost.setEntity(stringEntity);
                } else {
                    // 普通的form 表单
                    httpPost.setEntity(new UrlEncodedFormEntity(params, Consts.UTF_8));
                    httpPost.setConfig(requestConfig);
                }
            }
            StringBuilder cookieBuilder = new StringBuilder();
            if (headers != null && !headers.isEmpty()) {
                for (Map.Entry<String, String> entry : headers.entrySet()) {
                    httpPost.addHeader(entry.getKey(), entry.getValue());
                    cookieBuilder.append(entry.getKey())
                            .append("=")
                            .append(entry.getValue())
                            .append(";");
                }
            }

            httpPost.addHeader("Cookie", cookieBuilder.toString());
            //发送请求
            CloseableHttpResponse response = httpClient.execute(httpPost);
            try {
                // 执行POST请求,获取响应实体
                HttpEntity entity = response.getEntity();
                responseContent = getResultAndCloseHttpEntity(entity, charset);
            } finally {
                if (response != null) {
                    response.close();
                }
            }
        } catch (ClientProtocolException e) {
            logger.error("ClientProtocolException", e);
        } catch (IOException e) {
            logger.error("IOException", e);
        } finally {
            httpPost.releaseConnection();
        }
        long end = System.currentTimeMillis();
        logger.debug("请求[" + url + "]消耗时间 " + (end - start)
                + "毫秒");
        return responseContent;
    }

    public static String doGet(String url, Map<String, Object> params) {
        return doGet(url, params, null, null, CONNECT_TIMEOUT, SOCKET_TIMEOUT, null);
    }

    public static String doGet(String url, Map<String, Object> params, Map<String, String> headers) {
        return doGet(url, params, headers, null, CONNECT_TIMEOUT, SOCKET_TIMEOUT, null);
    }

    public static String doGet(String url, Map<String, Object> params, String referer) {
        return doGet(url, params, null, null, CONNECT_TIMEOUT, SOCKET_TIMEOUT, referer);
    }

    public static String doGet(String url, Map<String, Object> params, Map<String, String> headers, String referer) {
        return doGet(url, params, headers, null, CONNECT_TIMEOUT, SOCKET_TIMEOUT, referer);
    }

    /**
     * HTTP Get 获取内容
     *
     * @param url     请求的url地址 ?之前的地址
     * @param params  请求的参数，在浏览器？后面的数据，没有可以传null
     * @param charset 编码格式
     * @return 页面内容
     */
    /**
     * get 超时重试!
     * <p>
     * <p>
     * 对于非幂等的请求（比如 POST 方法），重试需要注意!
     *
     * @Retryable注解 被注解的方法发生异常时会重试
     * value:指定发生的异常进行重试
     * include:和value一样，默认空，当exclude也为空时，所有异常都重试
     * exclude:指定异常不重试，默认空，当include也为空时，所有异常都重试
     * maxAttemps:重试次数，默认3
     * backoff:重试补偿机制，默认没有
     * @Backoff注解 delay:指定延迟后重试
     * multiplier:指定延迟的倍数，比如delay=1000L,multiplier=2时，第一次重试为1秒后，第二次为2秒，第三次为3秒
     * @Recover 当重试到达指定次数时，被注解的方法将被回调，可以在该方法中进行日志处理。需要注意的是发生的异常和入参类型一致时才会回调
     */
    @Retryable(value = {SocketTimeoutException.class, ConnectionPoolTimeoutException.class}, backoff = @Backoff(delay = 1000L, multiplier = 3))
    public static String doGet(String url, Map<String, Object> params, Map<String, String> headers,
                               String charset, int connectTimeout, int socketTimeout, String referer) {
        if (StringUtils.isBlank(url)) {
            return null;
        }
        long start = System.currentTimeMillis();
        String responseContent = null;
        try {
            RequestConfig requestConfig = RequestConfig.custom()
                    .setSocketTimeout(socketTimeout)
                    .setConnectTimeout(connectTimeout)
                    .setConnectionRequestTimeout(connectTimeout).build();

            if (params != null && !params.isEmpty()) {
                List<NameValuePair> pairs = new ArrayList<>(params.size());
                for (Map.Entry<String, Object> entry : params.entrySet()) {
                    if (entry != null && entry.getValue() != null) {
                        String value = entry.getValue().toString();
                        pairs.add(new BasicNameValuePair(entry.getKey(), value));
                    }
                }
                url += "?" + EntityUtils.toString(new UrlEncodedFormEntity(pairs, charset));
            }

            logger.info("get url:{}", url);
            HttpGet httpGet = new HttpGet(url);
            StringBuilder cookieBuilder = new StringBuilder();
            if (headers != null && !headers.isEmpty()) {
                for (Map.Entry<String, String> entry : headers.entrySet()) {
                    httpGet.addHeader(entry.getKey(), entry.getValue());
                    cookieBuilder.append(entry.getKey())
                            .append("=")
                            .append(entry.getValue())
                            .append(";");
                }
            }

            httpGet.setHeader("referer", referer);
            httpGet.setConfig(requestConfig);
            httpGet.setHeader("Cookie", cookieBuilder.toString());
            CloseableHttpResponse response = httpClient.execute(httpGet);
            // 获取响应实体
            HttpEntity entity = response.getEntity();
            try {
                responseContent = getResultAndCloseHttpEntity(entity, charset);
            } finally {
                response.close();
            }
        } catch (ClientProtocolException e) {
            logger.error("ClientProtocolException", e);
        } catch (Exception e) {
            logger.error("Exception", e);
        } finally {

        }
        long end = System.currentTimeMillis();
        logger.debug("请求[" + url + "]消耗时间 " + (end - start)
                + "毫秒");
        return responseContent;

    }

    private static String getResultAndCloseHttpEntity(HttpEntity httpEntity, String charset) throws IOException {
        try {
            if (null != httpEntity) {
                return EntityUtils.toString(httpEntity, charset);
            }
        } finally {
            if (httpEntity != null) {
                httpEntity.getContent().close();
            }
        }
        return null;
    }
}