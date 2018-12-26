package com.qunar.qchat.utils;

import com.qunar.qchat.model.JsonResult;

public class JsonResultUtils {
//    private static final Logger LOGGER = LoggerFactory.getLogger(JsonResultUtils.class);

    public static JsonResult<?> success() {
        return success(null);
    }

    public static <T> JsonResult<T> success(T data) {
        return new JsonResult<>(JsonResult.SUCCESS, JsonResult.SUCCESS_CODE, null, data);
    }

    public static JsonResult<?> fail(int errcode, String errmsg) {
        return new JsonResult<>(JsonResult.FAIL, errcode, errmsg, null);
    }
}
