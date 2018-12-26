package com.qunar.qchat.aop;

import java.lang.annotation.*;

/**
 * Created by yhw on 06/29/2016.
 */
@Target({ElementType.METHOD,ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RecordAccessLog {
}
