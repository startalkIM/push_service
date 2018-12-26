package com.qunar.qchat.model.response;

import com.qunar.qchat.model.JsonBaseResult;

import java.util.List;

public class QchatVCardResult extends JsonBaseResult {

    public List<DataEntity> data;

    public static class DataEntity {
        public int displaytype;
        public int gender;
        public String imageurl;
        public String loginName;
        public String mobile;
        public String nickname;
        public String webname;
        public int type;
        public List<?> extentInfo;
        public String email;
        public String username;

    }
}
