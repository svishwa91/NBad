package com.assignment4.controllers;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
import com.assignment4.business.Mail;
import com.assignment4.business.User;
import com.assignment4.dao.userDAO;
import com.assignment4.dao.userDAOImpl;
import com.assignment4.security.PasswordUtil;
import com.assignment4.util.CommonUtility;
import com.assignment4.util.EmailUtil;
import com.assignment4.security.googleRecaptcha;
import datameer.com.google.common.cache.Cache;
import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 *
 * @author Jai Kiran
 */
@WebServlet(urlPatterns = {"/UserController"})
public class UserController extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        doPost(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession();
        String url = "/home.jsp";
        String action = CommonUtility.checkNullString(request.getParameter("action"));
        if (action.equals("")) {
            Cookie cookie1 = new Cookie("Host", request.getServerName());
            cookie1.setPath("/");
            Cookie cookie2 = new Cookie("port", Integer.toString(request.getServerPort()));
            cookie2.setPath("/");
            response.addCookie(cookie1);
            response.addCookie(cookie2);
            url = "/home.jsp";
        } else if (action.equals("login")) {
            String userName = CommonUtility.checkNullString(request.getParameter("email"));
            String password = CommonUtility.checkNullString(request.getParameter("password"));
            if (PasswordUtil.checkPasswordStrength(password)) {
                String gRecaptchaResponse = CommonUtility.checkNullString(request.getParameter("g-recaptcha-response"));
                boolean verify = googleRecaptcha.verify(gRecaptchaResponse);
                if (verify) {
                    User user = null;
                    userDAO udao = new userDAOImpl();
                    user = udao.valiadteLogin(userName, password);
                    if (user != null) {
                        String userType = CommonUtility.checkNullString(user.getType());
                        if (userType.equals("Participant")) {
                            session.setAttribute("theUser", user);
                            session.setAttribute("coins", user.getNumCoins());
                            session.setAttribute("NumParticipation", user.getNumParticipation());
                            url = "/main.jsp";
                        }
                        if (userType.equals("Admin")) {
                            session.setAttribute("theAdmin", user);
                            session.setAttribute("coins", user.getNumCoins());
                            session.setAttribute("NumParticipation", user.getNumParticipation());
                            url = "/admin.jsp";
                        }
                    } else {
                        request.setAttribute("msg", "Invalid User Name/Password");
                        url = "/login.jsp";
                    }
                } else {
                    request.setAttribute("msg", "You missed the Captcha");
                    url = "/login.jsp";
                }
            }else {
                    request.setAttribute("msg", "Invalid User Name/Password");
                    url = "/login.jsp";
            }
        } else if (action.equals("create")) {
            String Name = CommonUtility.checkNullString(request.getParameter("name"));
            String Email = CommonUtility.checkNullString(request.getParameter("email"));
            String password = CommonUtility.checkNullString(request.getParameter("password"));
            String confirmPassword = CommonUtility.checkNullString(request.getParameter("confirm_password"));
            if (PasswordUtil.checkPasswordStrength(password) && PasswordUtil.checkPasswordStrength(confirmPassword)) {
                String gRecaptchaResponse = CommonUtility.checkNullString(request.getParameter("g-recaptcha-response"));
                boolean verify = googleRecaptcha.verify(gRecaptchaResponse);
                if (verify) {
                    if (!Email.equals("") && password.equals(confirmPassword)) {
                        User user = new User();
                        user.setName(Name);
                        user.setEmail(Email);
                        user.setType("Participant");
                        user.setNumCoins(0);
                        user.setNumParticipation(0);
                        user.setNumPostedStudies(0);
                        userDAO udao = new userDAOImpl();
                        User checkUserExists = udao.getUser(Email);
                        if (checkUserExists == null) {
                            int count = udao.addUserRecord(user, password);
                            if (count == 1) {
                                session.setAttribute("theUser", user);
                                session.setAttribute("coins", user.getNumCoins());
                                session.setAttribute("NumParticipation", user.getNumParticipation());
                                url = "/main.jsp";
                            } else {
                                request.setAttribute("name", Name);
                                request.setAttribute("email", Email);
                                request.setAttribute("password", password);
                                request.setAttribute("confirmPassword", confirmPassword);
                                if (!Name.equals("") || !Email.equals("") || !password.equals("") || !confirmPassword.equals("")) {
                                    request.setAttribute("msg", "Error in Saving Data");
                                }
                                url = "/signup.jsp";
                            }
                        } else {
                            request.setAttribute("name", Name);
                            request.setAttribute("email", Email);
                            request.setAttribute("password", password);
                            request.setAttribute("confirmPassword", confirmPassword);
                            if (!Name.equals("") || !Email.equals("") || !password.equals("") || !confirmPassword.equals("")) {
                                request.setAttribute("msg", "User already Registered");
                            }
                            url = "/signup.jsp";
                        }
                    } else {
                        request.setAttribute("name", Name);
                        request.setAttribute("email", Email);
                        request.setAttribute("password", password);
                        request.setAttribute("confirmPassword", confirmPassword);
                        if (!Name.equals("") || !Email.equals("") || !password.equals("") || !confirmPassword.equals("")) {
                            request.setAttribute("msg", "Please fill all the details correctly");
                        }
                        url = "/signup.jsp";
                    }
                } else {
                    if (!Name.equals("") || !Email.equals("") || !password.equals("") || !confirmPassword.equals("")) {
                        request.setAttribute("msg", "You missed the Captcha");
                    }
                    url = "/signup.jsp";
                }
            }else{
                if (!Name.equals("") || !Email.equals("") || !password.equals("") || !confirmPassword.equals("")) {
                    request.setAttribute("msg", "Password Strength weak");
                }
                url = "/signup.jsp";
            }
        } else if (action.equals("how")) {
            if (session.getAttribute("theUser") != null) {
                url = "/main.jsp";
            } else if (session.getAttribute("theAdmin") != null) {
                url = "/admin.jsp";
            } else {
                url = "/how.jsp";
            }
        } else if (action.equals("about")) {
            if (session.getAttribute("theUser") != null || session.getAttribute("theAdmin") != null) {
                url = "/aboutl.jsp";
            } else {
                url = "/about.jsp";
            }
        } else if (action.equals("home")) {
            if (session.getAttribute("theUser") != null) {
                url = "/main.jsp";
            } else if (session.getAttribute("theAdmin") != null) {
                url = "/admin.jsp";
            } else {
                url = "/home.jsp";
            }
        } else if (action.equals("main")) {
            if (session.getAttribute("theUser") != null) {
                url = "/main.jsp";
            } else if (session.getAttribute("theAdmin") != null) {
                url = "/admin.jsp";
            } else {
                url = "/login.jsp";
            }
        } else if (action.equals("logout")) {
            if (session.getAttribute("theUser") != null || session.getAttribute("theAdmin") != null) {
                Cache<String, Boolean> csrfPreventionSaltCache = (Cache<String, Boolean>) request.getSession().getAttribute("csrfPreventionSaltCache");
                session.invalidate();
                session = request.getSession(true);
                session.setAttribute("csrfPreventionSaltCache", csrfPreventionSaltCache);

            }
            url = "/index.jsp";
        } else if (action.equals("mvToContact")) {
            if (session.getAttribute("theUser") != null) {
                url = "/contact.jsp";
            } else {
                url = "/login.jsp";
            }
        } else if (action.equals("contact")) {
            if (session.getAttribute("theUser") != null) {
                String toName = CommonUtility.checkNullString(request.getParameter("study_name"));
                String toEmail = CommonUtility.checkNullString(request.getParameter("email"));
                String Message = CommonUtility.checkNullString(request.getParameter("message"));
                String fromName = ((User) session.getAttribute("theUser")).getName();
                String fromEmail = ((User) session.getAttribute("theUser")).getEmail();
                Mail mail = new Mail();
                mail.setToName(toName);
                mail.setToEmail(toEmail);
                mail.setMessage(Message);
                mail.setFromName(fromName);
                mail.setFromEmail(fromEmail);
                mail.setMailType(action);
                mail.setSubject("Contact Request from " + fromName + " - Researchers Exchange Participation");
                boolean status = EmailUtil.sendMessage(mail);
                String msg = "";
                if (status) {
                    msg = "Message Sent. . .";
                } else {
                    msg = "Message Sending Failed. . .";
                }
                request.setAttribute("msg", msg);
                url = "/confirmc.jsp";
            } else {
                url = "/login.jsp";
            }
        } else if (action.equals("mvTorecommend")) {
            if (session.getAttribute("theUser") != null) {
                url = "/recommend.jsp";
            } else {
                url = "/login.jsp";
            }
        } else if (action.equals("recommendc")) {
            if (session.getAttribute("theUser") != null) {
                String toName = CommonUtility.checkNullString(request.getParameter("study_name"));
                String toEmail = CommonUtility.checkNullString(request.getParameter("friend_email"));
                //String friend_email=CommonUtility.checkNullString(request.getParameter("friend_email"));
                String Message = CommonUtility.checkNullString(request.getParameter("message"));
                String fromName = ((User) session.getAttribute("theUser")).getName();
                String fromEmail = ((User) session.getAttribute("theUser")).getEmail();
                Mail mail = new Mail();
                mail.setToName(toName);
                mail.setToEmail(toEmail);
                mail.setMessage(Message);
                mail.setFromName(fromName);
                mail.setFromEmail(fromEmail);
                mail.setMailType(action);
                mail.setSubject("Recommendation Recieved from " + fromName + " - Researchers Exchange Participation");
                boolean status = EmailUtil.sendMessage(mail);
                String msg = "";
                if (status) {
                    msg = "Recommendation Sent. . .";
                } else {
                    msg = "Recommendation Sent Failed. . .";
                }
                request.setAttribute("msg", msg);
                url = "/confirmr.jsp";
            } else {
                url = "/login.jsp";
            }
        } else {
            url = "/home.jsp";
        }
        getServletContext()
                .getRequestDispatcher(url)
                .forward(request, response);
    }

}
