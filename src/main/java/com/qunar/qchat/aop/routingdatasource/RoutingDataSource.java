package com.qunar.qchat.aop.routingdatasource;

import java.lang.annotation.*;


@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RoutingDataSource {

    DataSources value() default DataSources.QIM_SLAVE;
}
