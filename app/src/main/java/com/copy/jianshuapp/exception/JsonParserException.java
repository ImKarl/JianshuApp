package com.copy.jianshuapp.exception;

import com.copy.jianshuapp.modellayer.model.NoBody;
import com.copy.jianshuapp.modellayer.remote.JSResponse;
import com.copy.jianshuapp.modellayer.remote.RemoteManager;
import com.google.gson.JsonElement;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;

/**
 * JSON解析异常
 * @version imkarl 2016-08
 */
public class JsonParserException extends Exception {

    private JsonElement json;

    public JsonParserException(JsonElement json, String detailMessage) {
        super(detailMessage);
        this.json = json;
    }
    public JsonParserException(JsonElement json, Throwable throwable) {
        super(throwable);
        this.json = json;
    }
    public JsonParserException(Throwable throwable) {
        super(throwable);
    }

    @Override
    public String getDescription() {
        String description;

        // 获取响应结果
        NoBody response = null;
        try {
            response = getResponse(new TypeToken<NoBody>() {}.getType());
        } catch (Throwable ignored) { }

        if (response != null) {
            description = response.getErrorDescription();
        } else {
            description = getMessage();
        }

        return description;
    }

    @Override
    public String toString() {
        return super.toString() + "\n\tjson: " + json;
    }

    public JsonElement getJson() {
        return json;
    }
    public <T extends JSResponse> T getResponse(Type type) {
        return RemoteManager.getInstance().gson().fromJson(json, type);
    }

}