package com.qunar.qchat;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;
import javax.servlet.http.HttpSessionAttributeListener;
import javax.servlet.http.HttpSessionBindingEvent;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;

@WebListener
public class CustomServerletContextListener implements ServletContextListener,
        HttpSessionListener, HttpSessionAttributeListener {


    private static final Logger LOGGER = LoggerFactory.getLogger(CustomServerletContextListener.class);

    // Public constructor is required by servlet spec
    public CustomServerletContextListener() {
    }

    // -------------------------------------------------------
    // ServletContextListener implementation
    // -------------------------------------------------------
    public void contextInitialized(ServletContextEvent sce) {
      /* This method is called when the servlet context is
         initialized(when the Web application is deployed). 
         You can initialize servlet context related data here.
      */
        LOGGER.info("CustomServerletContextListener contextInitialized sce={}", sce);
    }

    public void contextDestroyed(ServletContextEvent sce) {
      /* This method is invoked when the Servlet Context 
         (the Web application) is undeployed or 
         Application Server shuts down.
      */
        LOGGER.info("CustomServerletContextListener contextDestroyed sce={}", sce);
    }

    // -------------------------------------------------------
    // HttpSessionListener implementation
    // -------------------------------------------------------
    public void sessionCreated(HttpSessionEvent se) {
      /* Session is created. */
        LOGGER.info("CustomServerletContextListener sessionCreated se={}", se);
    }

    public void sessionDestroyed(HttpSessionEvent se) {
      /* Session is destroyed. */
        LOGGER.info("CustomServerletContextListener sessionDestroyed se={}", se);
    }

    // -------------------------------------------------------
    // HttpSessionAttributeListener implementation
    // -------------------------------------------------------

    public void attributeAdded(HttpSessionBindingEvent sbe) {
      /* This method is called when an attribute 
         is added to a session.
      */
        LOGGER.info("CustomServerletContextListener attributeAdded sbe={}", sbe);
    }

    public void attributeRemoved(HttpSessionBindingEvent sbe) {
      /* This method is called when an attribute
         is removed from a session.
      */
        LOGGER.info("CustomServerletContextListener attributeRemoved sbe={}", sbe);
    }

    public void attributeReplaced(HttpSessionBindingEvent sbe) {
      /* This method is invoked when an attibute
         is replaced in a session.
      */
        LOGGER.info("CustomServerletContextListener attributeReplaced sbe={}", sbe);
    }
}
