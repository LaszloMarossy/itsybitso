package com.itsybitso.controller;

import com.itsybitso.executor.TradesPoller;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

@WebListener
public class StartupListener implements ServletContextListener {

    @Override
    public void contextInitialized(ServletContextEvent servletContextEvent) {
      TradesPoller.getInstance().getXchangeRate();
      TradesPoller.getInstance().getRecentBitsoTrades();
      System.out.println("Starting up!");
    }

    @Override
    public void contextDestroyed(ServletContextEvent servletContextEvent) {
        System.out.println("Shutting down!");
    }
}