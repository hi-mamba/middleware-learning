
<http://natehsu.blogspot.com/2019/09/jsonrpc4j-error-code-32602-method.html>

# 解決 jsonrpc4j error code -32602 method parameters invalid 的錯誤

jsonrpc4j 會分析 input params 與 java method params 是不是一致，它會檢查 params 數量、名稱，如果沒有 mapping 就拋出錯誤了。

我們可以從 com.googlecode.jsonrpc4j.JsonRpcBasicServer Class 查看到，裡面的
private AMethodWithItsArgs findBestMethodByParamsNode(Set<Method> methods, JsonNode paramsNode) 就是在處理這塊，
 視 input 資料結構分別呼叫 findBestMethodUsingParamIndexes 或 findBestMethodUsingParamNames 處理。
