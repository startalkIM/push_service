package com.qunar.qchat.dao;

import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Component;

import java.util.HashMap;

@Component
public interface IHostUserDao {
    public String selectName(
            @Param("table") String table,
            @Param("join") String host_info,
            @Param("user_name") String user_name,
            @Param("host") String host);
    public String selectMucName(
            @Param("table") String table,
            @Param("muc_name") String muc_name);

}