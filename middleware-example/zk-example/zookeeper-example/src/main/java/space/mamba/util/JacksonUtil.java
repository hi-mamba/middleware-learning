package space.mamba.util;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.type.TypeFactory;
import org.apache.commons.lang3.StringUtils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * json工具
 *
 * @author pankui
 */
public final class JacksonUtil {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    static {
        // 序列化时，跳过null属性
        MAPPER.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        // 序列化时，遇到空bean（无属性）时不会失败
        MAPPER.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        // 反序列化时，遇到未知属性（在bean上找不到对应属性）时不会失败
        MAPPER.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        // 反序列化时，将空数组([])当做null来处理（以便把空数组反序列化到对象属性上——对php生成的json的map属性很有用）
        MAPPER.configure(DeserializationFeature.ACCEPT_EMPTY_ARRAY_AS_NULL_OBJECT, true);
        // 反序列化时，是否允许JSON字符串包含非引号控制字符（值小于32的ASCII字符，包含制表符和换行符）
        MAPPER.configure(JsonParser.Feature.ALLOW_UNQUOTED_CONTROL_CHARS, true);
        //反序列化时，该是否允许解析使用Java/C++ 样式的注释（包括'/'+'*' 和'//' 变量）。
        MAPPER.configure(JsonParser.Feature.ALLOW_COMMENTS, true);
        // 反序列化时，是否将允许使用非双引号属性名字
        MAPPER.configure(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true);
        // 反序列化时，是否允许单引号来包住属性名称和字符串值
        MAPPER.configure(JsonParser.Feature.ALLOW_SINGLE_QUOTES, true);
        // 序列化与反序列化日期时，使用标准格式
        MAPPER.setDateFormat(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"));

    }

    private JacksonUtil() {
    }

    public static ObjectMapper getMapperInstance() {
        return MAPPER;
    }

    public static String toJSon(Object object) {
        if (object == null) {
            return "";
        }
        try {
            return MAPPER.writeValueAsString(object);
        } catch (Exception e) {
            throw new RuntimeException("not able to convert object to json");
        }
    }

    public static <T> T[] readValue2Array(String jsonStr, Class<T> tClass) {
        if (StringUtils.isBlank(jsonStr)) {
            return (T[]) new Object[0];
        }
        try {
            return (T[]) MAPPER.readValue(jsonStr, TypeFactory.defaultInstance().constructArrayType(tClass));
        } catch (Exception e) {
            throw new RuntimeException("not able to convert json string:" + jsonStr);
        }
    }

    public static <T> List<T> readValue2List(String jsonStr, Class<T> tClass) {
        if (StringUtils.isBlank(jsonStr)) {
            return new ArrayList<>();
        }
        try {
            return MAPPER.readValue(jsonStr,
                    TypeFactory.defaultInstance().constructCollectionLikeType(List.class, tClass));
        } catch (Exception e) {
            throw new RuntimeException("not able to convert json string:" + jsonStr);
        }
    }

    public static <K, V> Map<K, V> readValue2Map(String jsonStr, Class<K> keyClass, Class<V> valueClass) {
        if (StringUtils.isBlank(jsonStr)) {
            return new HashMap<>();
        }
        try {
            return MAPPER.readValue(jsonStr,
                    TypeFactory.defaultInstance().constructMapLikeType(Map.class, keyClass, valueClass));
        } catch (Exception e) {
            throw new RuntimeException("not able to convert json string:" + jsonStr);
        }
    }

    public static <T> T readValue2Bean(String jsonStr, TypeReference<T> valueTypeRef) {
        if (StringUtils.isBlank(jsonStr)) {
            return null;
        }
        try {
            return MAPPER.readValue(jsonStr, valueTypeRef);
        } catch (Exception e) {
            throw new RuntimeException("not able to convert json string:" + jsonStr);
        }
    }

    public static <T> T readValue2Bean(String jsonStr, Class<T> tClass) {
        if (StringUtils.isBlank(jsonStr)) {
            return null;
        }
        try {
            return MAPPER.readValue(jsonStr, tClass);
        } catch (Exception e) {
            throw new RuntimeException("not able to convert json string:" + jsonStr);
        }
    }

    public static JsonNode toNode(String jsonString) {
        return readValue2Bean(jsonString, JsonNode.class);
    }
}
