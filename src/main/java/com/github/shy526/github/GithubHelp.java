package com.github.shy526.github;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.github.shy526.factory.OkHttpClientFactory;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.apache.commons.codec.binary.Base64;

import java.io.IOException;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
public class GithubHelp {
    private static final String CREATE_UPDATE_PATH = "/repos/%s/contents/src/main/resources/%s";
    private static final String GITHUB_HOST = "https://api.github.com%s";
    private static Request buildPutReq(String url,String bodyStr,String token) {
        RequestBody body = RequestBody.create(
                MediaType.parse("application/json"),
                bodyStr
        );
        return new Request.Builder()
                .url(url)
        .addHeader("Accept", "application/vnd.github+json")
        .addHeader("Host", "api.github.com")
                .addHeader("Authorization", "token " + token)
                .put(body)
                .build();
    }
    private static Request buildReq(String url,String token) {

        return new Request.Builder()
                .url(url)
                .addHeader("Accept", "application/vnd.github+json")
                .addHeader("Host", "api.github.com")
                .addHeader("Authorization", "token " + token)
                .build();
    }
    public static void createOrUpdateFile(String content,String fileName,String ownerRepo,String token) {
        OkHttpClient client = OkHttpClientFactory.getInstance().getClient();
        String base64Content = Base64.encodeBase64String(content.getBytes());
        JSONObject contentJson = getContent(fileName,ownerRepo,token);
        JSONObject jsonObject = new JSONObject();
        if (!contentJson.isEmpty()){
            jsonObject.put("sha",contentJson.getString("sha"));
        }
        jsonObject.put("content",base64Content);
        jsonObject.put("message",fileName+"---update");
        jsonObject.put("path",base64Content);

        String  url = String.format(GITHUB_HOST,  String.format(CREATE_UPDATE_PATH,ownerRepo,fileName));
        Request request = buildPutReq(url,jsonObject.toJSONString(),token);

        try (Response response = client.newCall(request).execute()) {

        } catch (IOException ignored) {
        }
    }


    public static JSONObject getContent(String fileName,String ownerRepo,String token) {
        OkHttpClient client = OkHttpClientFactory.getInstance().getClient();
        String  url = String.format(GITHUB_HOST,  String.format(CREATE_UPDATE_PATH,ownerRepo,fileName));
        Request request = buildReq(url,token);
        try (Response response = client.newCall(request).execute()) {
            JSONObject jsonObject = JSON.parseObject(response.body().string());
            Integer status = jsonObject.getInteger("status");
            if (status!=null&&status == 404) {
                return new JSONObject();
            }
            return jsonObject;
        } catch (IOException ignored) {

        }
        return  new JSONObject();
    }

}
