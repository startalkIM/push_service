package com.qunar.qchat.dao;

import com.qunar.qchat.dao.model.UserModule;
import com.qunar.qchat.dao.model.VirtualUserModule;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Created by qitmac000378 on 17/5/23.
 */
@Component
public interface IUserDao {
    public List<UserModule> selectAllUser();
    public VirtualUserModule getVirtualUser(
            @Param("username") String fuser
    );
}
