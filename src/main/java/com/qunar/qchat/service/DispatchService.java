package com.qunar.qchat.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;

/**
 * create by hubo.hu (lex) at 2018/3/22
 */
@Service
public class DispatchService {

    @Autowired
    private QTalkVCardService qTalkVCardService;

    private HashMap<String, QVCardService> serviceMap = new HashMap<>();


    public QVCardService getServiceByDomain(String fromhost) {
        if(serviceMap != null && serviceMap.size() == 0){
            serviceMap.put("ejabhost1", qTalkVCardService);
            serviceMap.put("conference.ejabhost1", qTalkVCardService);
        }
        QVCardService service = null;
        if(serviceMap != null && serviceMap.containsKey(fromhost)){
            service = serviceMap.get(fromhost);
        }
        if(service == null){
            return qTalkVCardService;
        }
        return service;
    }
}
