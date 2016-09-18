/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.assignment4.dao;

import com.assignment4.business.User;

/**
 *
 * @author Jai Kiran
 */
public interface userDAO {
    
    public User getUser(String Email);
    public int addUserRecord(User user,String password);
    public User valiadteLogin(String userName,String password);
}
