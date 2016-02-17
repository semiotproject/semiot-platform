<%@page import="ru.semiot.platform.apigateway.config.QueryUtils"%>

<%
    boolean managerIsConfigurated = true;
    boolean isConnect = true;
    try {
        managerIsConfigurated = QueryUtils.managerIsConfigurated();
    } catch (Exception ex) {
        ex.printStackTrace();
        isConnect = false;
    }
%>

<%@ page language="java" contentType="text/html; charset=UTF-8"
         pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
    <head>
        <title>SemIoT Platform</title>
        <meta charset="utf-8">
        <meta name="viewport" content="width=device-width, initial-scale=1">
        <link rel="stylesheet"
              href="http://maxcdn.bootstrapcdn.com/bootstrap/3.3.5/css/bootstrap.min.css">
        <script
        src="https://ajax.googleapis.com/ajax/libs/jquery/1.11.3/jquery.min.js"></script>
        <script
        src="http://maxcdn.bootstrapcdn.com/bootstrap/3.3.5/js/bootstrap.min.js"></script>
    </head>
    <body>
        <div class="container">
            <h1>SemIoT Platform</h1>
            <%
                if (isConnect) {
            %>
            <b>Navigation:</b>
            <ul class="nav nav-pills nav-justified">
                <a href="/explorer">Go to Explorer</a> |
                <% if (!managerIsConfigurated) { %>
                <a href="/config/SystemSettings">Go to Configuration</a>
                <% } else { %>
                <a href="/config/DriversInstalled">Go to Configuration</a>
                <% } %>
            </ul>
            <%
            } else {
            %>
            <b>Can`t connect to webconsole. Please reload the page after a few
                seconds.</b>
                <%
                    }
                %>
        </div>
    </body>
</html>
