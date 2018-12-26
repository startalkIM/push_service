package com.qunar.qchat.aop;

/**
 * AOP切面的执行顺序, 由小到大执行.
 * 如某方法m有3个切面, 都是Around类型, Order分别为1,2,3.
 * 则m方法的执行顺序为 1,2,3,m,3,2,1
 *
 * @author chengya.li on 2014-06-27 16:58
 */
public interface AspectOrder {

    /**
     * 业务监控
     */
    int CERBERUS = -1;

    /**
     * 缓存, see file memoryCache.xml
     * <pre>
     *   <bean class="com.google.code.ssm.Settings">
     *     <property name="order" value="1" />
     *   </bean>
     * </pre>
     */
    int CACHE = 1;

    /**
     * 动态数据源
     */
    int ROUTING_DATA_SOURCE = 100;

    /**
     * 事务, see file applicationContext-base.xml
     * <pre>
     *   <tx:annotation-driven transaction-manager="transactionManager" proxy-target-class="true" order="200" />
     * </pre>
     * @see org.springframework.transaction.annotation.Transactional
     */
    int TRANSACTIONAL = 200;
    
    int SENSITIVE = 300;

}
