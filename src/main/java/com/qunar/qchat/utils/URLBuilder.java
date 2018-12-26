package com.qunar.qchat.utils;

import org.apache.http.util.TextUtils;

/**
 * Created by qitmac000378 on 17/8/31.
 */

public class URLBuilder {
    private String host;
    private StringBuilder queryBuilder;

    public static URLBuilder builder() {
        return new URLBuilder();
    }

    public URLBuilder setHost(String host) {
        this.host = host;
        return this;
    }

    public URLBuilder addQuery(String key, String value) {
        if (TextUtils.isEmpty(key))
            return this;
        if (TextUtils.isEmpty(value))
            return this;
        if (null == queryBuilder)
            queryBuilder = new StringBuilder();
        if (0 != queryBuilder.length())
            queryBuilder.append("&");

        queryBuilder.append(key);
        queryBuilder.append("=");
        queryBuilder.append(value);
        return this;
    }

    public URLBuilder addQuery(String key, boolean value) {
        if (TextUtils.isEmpty(key))
            return this;
        if (null == queryBuilder)
            queryBuilder = new StringBuilder();
        if (0 != queryBuilder.length())
            queryBuilder.append("&");

        queryBuilder.append(key);
        queryBuilder.append("=");
        queryBuilder.append(value);
        return this;
    }

    public URLBuilder addQuery(String key, int value) {
        if (TextUtils.isEmpty(key))
            return this;

        if (null == queryBuilder)
            queryBuilder = new StringBuilder();
        if (0 != queryBuilder.length())
            queryBuilder.append("&");

        queryBuilder.append(key);
        queryBuilder.append("=");
        queryBuilder.append(value);
        return this;
    }

    public String build() {

        if (TextUtils.isEmpty(host))
            return "";
        if (null!= queryBuilder && queryBuilder.length() > 0)
            return host + "?" + queryBuilder.toString();
        else
            return host;
    }
}
