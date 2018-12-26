package com.qunar.qchat.aop.routingdatasource;

import com.qunar.qchat.aop.AspectOrder;
import com.qunar.qchat.utils.CustomRuntimeException;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.lang.reflect.Method;

/**
 * 动态数据源Advisor
 *
 * @author chengya.li on 14-6-23 11:26
 */
@Component
@Aspect
@Order(AspectOrder.ROUTING_DATA_SOURCE)
public class RoutingDataSourceAdvisor {

    private final static Logger logger = LoggerFactory.getLogger(RoutingDataSourceAdvisor.class);

    // 只对*Service类里标注了@RoutingDataSource 注解方法 进行AOP
    //@Pointcut("execution(@com.qunar.qchat.aop.routingdatasource.RoutingDataSource * com.qunar.qchat..*Service+.*(..))")
    @Pointcut("@annotation(com.qunar.qchat.aop.routingdatasource.RoutingDataSource)")
    private void routingDataSource(){}

    @Around("routingDataSource()")
    public Object routing(ProceedingJoinPoint joinPoint) throws CustomRuntimeException {
        Class<?> clazz = joinPoint.getTarget().getClass();
        String className = clazz.getName();
        Method method = ((MethodSignature)joinPoint.getSignature()).getMethod();
        String methodName = method.getName();
        Object[] arguments = joinPoint.getArgs();
        RoutingDataSource routingDataSource = method.getAnnotation(RoutingDataSource.class);
        String key = routingDataSource.value().key();
        long time1 = System.currentTimeMillis();
//        logger.info("Routing to datasource({}) in {}.{}, args={}", key, className, methodName, arguments);
        long time2 = System.currentTimeMillis();
        DataSourceKeyHolder.set(key);

        long time3 = System.currentTimeMillis();

        if ((time3 - time1) > 10 * 1000L) {
            logger.warn("RoutingDataSourceAdvisor切换数据源超时,className={},methodName={},key={},耗时={}ms", className, methodName,key,
                    (time3 - time1));
            logger.warn("RoutingDataSourceAdvisor切换数据源超时,key={},time1={},time2={},time3={}",key,time1, time2,time3);
        }

        Object result = null;
        try {
            checkIfCompatible(clazz, method);
            result = joinPoint.proceed(arguments);
        } catch (Throwable e) {
            logger.error("Error occurred during datasource(key=" + key + ") routing, ", e);
        } finally {
            DataSourceKeyHolder.clear();
        }
        return result;
    }

    private void checkIfCompatible(Class<?> clazz, Method method) {
        if (DataSourceKeyHolder.isNestedCall()){
            Transactional transactional = method.getAnnotation(Transactional.class);
            if(transactional == null){
                transactional = clazz.getAnnotation(Transactional.class);
            }
            if(transactional != null){
                if(transactional.propagation() != Propagation.REQUIRES_NEW){
                    throw new CustomRuntimeException("@RoutingDataSource方法嵌套调用时, 如果内部方法具有@Transactional注解, 必须定义为propagation = REQUIRES_NEW");
                }
            }
        }
    }

}
