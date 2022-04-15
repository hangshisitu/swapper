package com.aicoding.swapper;

import com.alibaba.fastjson.JSONObject;
import graphql.kickstart.spring.webclient.boot.GraphQLRequest;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * graphql工具类
 *
 * @Date: 2022/4/15 18:45
 * @Author: qiaojun.xiao
 * @Copyright（C）: 2019-2022 ZP Inc.   All rights reserved.
 * 注意：本内容仅限于佐朋数科内部传阅，禁止外泄以及用于其他的商业目的。
 */
@Component
@Slf4j
public class GraphQlUtils {

    private static OkHttpClient okHttpClient = new OkHttpClient().newBuilder()
            .connectTimeout(60, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(65, TimeUnit.SECONDS)
            .build();

    private static final String url ="https://api.thegraph.com/subgraphs/name/uniswap/uniswap-v2";

    public <T> T graphQLQuery(String ql,Object variables,Class<T> clazz)
    {
        GraphQLRequest graphqlRequest = GraphQLRequest.builder()
                .resource(ql)
                .variables(variables)
                .build();

        RequestBody requestBody = RequestBody.create(MediaType.parse("application/json;charset=utf-8;"),
                JSONObject.toJSONString(graphqlRequest.getRequestBody()));
        Request request = new Request.Builder().url(url).post(requestBody).build();
        return execute(request,clazz);
    }

    private <T> T execute(Request request,Class<T> clazz)
    {
        try {
            log.info("request {}",request.body().toString());
            Response response = okHttpClient.newCall(request).execute();
            if (response.isSuccessful()) {
                String text = response.body().string();
                log.info("crawl response body {}",text);
                return JSONObject.parseObject(text).getJSONObject("data").toJavaObject(clazz);
            }
            log.error("接口失败 resp {}",response);
            throw new RuntimeException("接口失败");
        }catch (Exception e)
        {
            log.error("调用接口出错",e);
            throw new RuntimeException("调用接口出错");
        }
    }

}