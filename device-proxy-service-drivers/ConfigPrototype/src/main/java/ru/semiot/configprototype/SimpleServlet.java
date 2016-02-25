/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ru.semiot.configprototype;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.felix.webconsole.AbstractWebConsolePlugin;

/**
 *
 * @author Daniil Garayzuev <garayzuev@gmail.com>
 */
public class SimpleServlet extends AbstractWebConsolePlugin{
    
    public static final String TITLE = "simpleServlet";
    public static final String LABEL = "hello";
    
    @Override
    protected void renderContent(HttpServletRequest hsr, HttpServletResponse hsr1) throws ServletException, IOException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public String getLabel() {
        return LABEL;
    }

    @Override
    public String getTitle() {
        return TITLE;
    }
    
}
