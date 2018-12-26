package com.qunar.qchat.interceptor;

import com.qunar.qtalk.ss.common.utils.watcher.QMonitor;
import com.qunar.qchat.constants.QMonitorConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class MonitorHandlerExceptionResolver implements HandlerExceptionResolver {
    private static final Logger LOGGER = LoggerFactory.getLogger(MonitorHandlerExceptionResolver.class);

    @Override
    public ModelAndView resolveException(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        QMonitor.recordOne(QMonitorConstants.GLOBAL_ERROR);
        LOGGER.error("has error", ex);
        return null;
    }
}
