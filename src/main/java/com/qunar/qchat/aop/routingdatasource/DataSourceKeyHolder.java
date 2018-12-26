package com.qunar.qchat.aop.routingdatasource;

import java.util.LinkedList;

/**
 * 注意：这里的holder中的数据源key，只在被spring托管的dao类的执行时生效。在未被spring托管的比如使用MybatisUtil执行的
 * 保存一个线程中的动态数据源的key
 *
 * @author chengya.li on 2014-06-23 15:35
 */
public class DataSourceKeyHolder {


    public static final ThreadLocal<LinkedList<String>> holder = new ThreadLocal<LinkedList<String>>() {
        @Override
        protected LinkedList<String> initialValue() {
            return new LinkedList<String>();
        }
    };

    public static void set(String key) {
        holder.get().push(key);
    }

    public static void clear() {
        holder.get().pop();
    }

    public static void clearAll() {
        holder.get().clear();
    }

    public static String getCurrentKey() {
        if (holder.get().size() == 0)
            return null;
        return holder.get().getFirst();
    }

    public static boolean isNestedCall() {
        return holder.get().size() > 1;
    }


    /**
     * 切换数据源为指定的数据源
     * @param dataSource
     */
    public static void switchDataSource(DataSources dataSource){
        set(dataSource.key());
    }

}
