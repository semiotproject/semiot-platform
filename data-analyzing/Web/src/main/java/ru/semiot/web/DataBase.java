/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ru.semiot.web;

/**
 *
 * @author Daniil Garayzuev <garayzuev@gmail.com>
 */
public interface DataBase {
    public void appendRequest(String request);
    public int getCount();
    public String getRequest(int id);
    public void removeRequest(int id);
}
