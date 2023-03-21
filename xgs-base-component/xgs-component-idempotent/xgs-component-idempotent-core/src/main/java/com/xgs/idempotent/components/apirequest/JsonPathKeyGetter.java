package com.xgs.idempotent.components.apirequest;

import com.xgs.idempotent.components.IdempotentKeyGetter;
import com.xgs.idempotent.components.IdempotentRequestContext;
import com.xgs.idempotent.constants.IdempotentErrorCodeEnum;
import com.xgs.idempotent.exception.IdempotentException;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.spi.json.JacksonJsonProvider;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang3.StringUtils;


/**
 * JsonPath 幂等键获取器
 * @author xiongguoshuang
 */
public class JsonPathKeyGetter implements IdempotentKeyGetter, ApiRequestSupport {


    /**
     * JsonPath 表达式
     */
    private final List<String> jsonPathExpressionList;

    /**
     * JsonPath 表达式  形如  $.store.book[0].author
     * 关于jsonPath表达式更多格式，请自行百度
     * @param jsonPathExpressionList
     */
    public JsonPathKeyGetter(String... jsonPathExpressionList){
        if(jsonPathExpressionList == null || jsonPathExpressionList.length ==0){
            throw IdempotentException.error(
                    IdempotentErrorCodeEnum.PARAM_ERROR.getCode(),
                    "jsonPathExpressionList 不能为空"
            );
        }
        if(jsonPathExpressionList.length > 4){
            throw IdempotentException.error(
                    IdempotentErrorCodeEnum.PARAM_ERROR.getCode(),
                    "jsonPathExpressionList 不能超过4个"
            );
        }

        this.jsonPathExpressionList = new ArrayList<>();
        for (String jsonPathExpression : jsonPathExpressionList) {
            if(StringUtils.isBlank(jsonPathExpression)){
                throw IdempotentException.error(
                        IdempotentErrorCodeEnum.PARAM_ERROR.getCode(),
                        "attributePath 不能为空"
                );
            }
            this.jsonPathExpressionList.add(jsonPathExpression);
        }

    }

    private final JacksonJsonProvider jacksonJsonProvider = new JacksonJsonProvider();

    @Override
    public String parseRecordKey(IdempotentRequestContext requestContext) {
        Object data = this.parseApiRequestDataFromParams(requestContext);
        String json = jacksonJsonProvider.toJson(data);

        StringBuilder sb = new StringBuilder();
        for (String jsonPathExpression : jsonPathExpressionList) {
            String jsonPathExpressionResult = readJson(json, jsonPathExpression);
            sb.append(jsonPathExpressionResult).append("|");
        }
        if(sb.length()>0){
            // 删掉最后一个|
            sb.deleteCharAt(sb.length()-1);
        }
        return sb.toString();
    }


    private String readJson(String json, String jsonPathExpression){
        Object read = JsonPath.read(json, jsonPathExpression);
        return read == null ? "" : read.toString();
    }
}
