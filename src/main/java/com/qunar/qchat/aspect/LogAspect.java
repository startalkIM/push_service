package com.qunar.qchat.aspect;

import com.alibaba.fastjson.JSON;
import com.qunar.qchat.utils.DateUtil;
import com.qunar.qchat.utils.JacksonUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

/**
 * 日志记录AOP实现
 */
@Component
@Aspect
public class LogAspect {
    private final Logger pushInfoAppender = LoggerFactory.getLogger("pushInfoAppender");

    @Pointcut("@annotation(com.qunar.qchat.aop.RecordAccessLog)")
    public void inWebLayer(){
    }

    @Around("@annotation(com.qunar.qchat.aop.RecordAccessLog)")
    public Object doAroundInControllerLayer(ProceedingJoinPoint pjp)  {
//        boolean isRecord = Boolean.parseBoolean(Config.getPropertyInQConfig(ConfigConstants.ASSIGN_SEAT_LOG_SWITCH, "true"));
//        if (!isRecord) {
//            return pjp.proceed();
//        }
        long startTimeMillis = System.currentTimeMillis(); // 开始时间
        RequestAttributes ra = RequestContextHolder.getRequestAttributes();
        ServletRequestAttributes sra = (ServletRequestAttributes)ra;
        HttpServletRequest request = sra.getRequest();
        String userName = "";// AuthorityUtil.getThirdPartyUserName(request);
        Map<String, String[]> inputParamMap = request.getParameterMap();
        String requestPath = request.getRequestURI();
        Object result = null;
        try {
            result = pjp.proceed();
        } catch (Throwable throwable) {
            pushInfoAppender.error(throwable + "");
        }
        long endTimeMillis = System.currentTimeMillis(); // 结束时间
        String agent = request.getHeader("User-Agent");
        String optTime = DateUtil.longToString(startTimeMillis, "yyyy-MM-dd HH:mm:ss");
        pushInfoAppender.info("登录用户:{}, 请求地址: {}, UA: {}, 请求时间:{}, 用时: {}, 输入:{}, 输出:{}",userName, requestPath, agent, optTime,
                (endTimeMillis - startTimeMillis) + "ms", JSON.toJSONString(inputParamMap), JSON.toJSONString(result));
        return result;
    }
}

