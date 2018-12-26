/*     Copyright (c) 2017, jymenu.com. All rights reserved.   */
package com.qunar.qchat.service;

import com.notnoop.apns.ApnsNotification;
import com.notnoop.apns.ApnsService;
import com.notnoop.apns.internal.Utilities;
import com.notnoop.exceptions.NetworkIOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 *
 */
public class QueuedApnsService {

    private static final Logger logger = LoggerFactory.getLogger(QueuedApnsService.class);

    private ApnsService service;
    private BlockingQueue<ApnsNotification> queue;
    private LinkedHashMap<String, Long> sendTimeCache;
    private AtomicBoolean started = new AtomicBoolean(false);

    public QueuedApnsService(ApnsService service) {
        this(service, null);
    }

    public QueuedApnsService(ApnsService service, final ThreadFactory tf) {
        this.service = service;
        this.queue = new LinkedBlockingQueue<ApnsNotification>();
        this.sendTimeCache = new LinkedHashMap<>(10000);
        this.threadFactory = tf == null ? Executors.defaultThreadFactory() : tf;
        this.thread = null;
    }

    public void push(ApnsNotification msg) {
        if (!started.get()) {
            throw new IllegalStateException("service hasn't be started or was closed");
        }
        queue.add(msg);
    }

    private final ThreadFactory threadFactory;
    private Thread thread;
    private volatile boolean shouldContinue;

    public void start() {
        if (started.getAndSet(true)) {
            // I prefer if we throw a runtime IllegalStateException here,
            // but I want to maintain semantic backward compatibility.
            // So it is returning immediately here
            return;
        }

        service.start();
        shouldContinue = true;
        thread = threadFactory.newThread(new Runnable() {
            public void run() {
                while (shouldContinue) {
                    try {
                        long nowtime = System.currentTimeMillis();
                        ApnsNotification msg = queue.take();
                        if(!sendTimeCache.containsKey(Utilities.encodeHex(msg.getDeviceToken()))) {
                            service.push(msg);
                            sendTimeCache.put(Utilities.encodeHex(msg.getDeviceToken()), nowtime);
                        } else {

                            if(nowtime - sendTimeCache.get(Utilities.encodeHex(msg.getDeviceToken())) > 2000) {
                                service.push(msg);
                                sendTimeCache.put(Utilities.encodeHex(msg.getDeviceToken()), System.currentTimeMillis());
                            } else {
                                queue.add(msg);
                            }
                        }
                    } catch (InterruptedException e) {
                        // ignore
                    } catch (NetworkIOException e) {
                        // ignore: failed connect...
                    } catch (Exception e) {
                        // weird if we reached here - something wrong is happening, but we shouldn't stop the service anyway!
                        logger.warn("Unexpected message caught... Shouldn't be here", e);
                    }
                }
            }
        });
        thread.start();
    }

    public void stop() {
        started.set(false);
        shouldContinue = false;
        thread.interrupt();
        service.stop();
    }

    public Map<String, Date> getInactiveDevices() throws NetworkIOException {
        return service.getInactiveDevices();
    }

    public void testConnection() throws NetworkIOException {
        service.testConnection();
    }


}