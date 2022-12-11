package controller;

import db.DataBase;
import model.User;
import util.HttpRequestUtils;
import webserver.HttpRequest;
import webserver.HttpResponse;

import java.util.Collection;
import java.util.Map;

public class ListUserController implements Controller{
    public void service(HttpRequest request, HttpResponse response) {
        if(isLogin(request.getParameter("Cookie"))) {
            Collection<User> users =  DataBase.findAll();
            response.forwardBody(users.toString());
        } else {
            response.forward("/user/login.html");
        }
    }


    public boolean isLogin(String cookie) {
        Map<String, String> cookies = HttpRequestUtils.parseCookies(cookie);
        if(cookies.containsKey("logined") && Boolean.parseBoolean(cookies.get("logined")))
            return true;
        else
            return false;
    }
}
