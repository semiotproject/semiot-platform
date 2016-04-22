package ru.semiot.platform.apigateway.config;

import ru.semiot.platform.apigateway.beans.impl.OSGiApiService;
import rx.Observable;

import java.io.IOException;

import javax.inject.Inject;
import javax.servlet.AsyncContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet(urlPatterns = "/config", asyncSupported = true)
public class ConfigHandler extends HttpServlet {

  @Inject
  private OSGiApiService service;

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {

    final AsyncContext ctx = req.startAsync();

    Observable<Boolean> managerIsConfigurated = service.managerIsConfigurated();

    managerIsConfigurated.subscribe((Boolean isConf) -> {
      if (isConf) {
        ctx.dispatch("/config/DriversInstalled");
      } else {
        ctx.dispatch("/config/SystemSettings");
      }
    }, (Throwable e) -> ctx.dispatch("/notConnect"), () -> {});
  }
}
