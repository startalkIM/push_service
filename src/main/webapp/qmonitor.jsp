<%@ page session="false" %>

<%@page import="com.qunar.qtalk.ss.common.utils.watcher.QMonitor" %><%@

page import="java.util.Map.Entry"%>
<%@ page contentType="text/plain;charset=UTF-8" language="java" %><%


for (Entry<String, Long> entry : QMonitor.getValues().entrySet()) {

String name = entry.getKey();

Number value = entry.getValue();

out.append(name + "=" + value.longValue() + "\n");

}

%>