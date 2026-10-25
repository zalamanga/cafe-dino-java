package com.dinocafe;

import javax.servlet.*;
import javax.servlet.annotation.WebListener;

@WebListener
public class Lifecycle implements ServletContextListener {
  @Override
  public void contextDestroyed(ServletContextEvent event) {
    Database.close();
  }
}
