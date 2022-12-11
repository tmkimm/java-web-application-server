package controller;

import db.DataBase;
import model.User;
import util.HttpRequestUtils;
import webserver.HttpRequest;
import webserver.HttpResponse;

import java.util.Collection;
import java.util.Map;

public class ListUserController extends AbstractController{
    @Override
    public void service(HttpRequest request, HttpResponse response) {
        super.service(request, response);
    }

    @Override
    public void doGet(HttpRequest request, HttpResponse response) {
        Map<String, String> header = request.getHeaders();
        if(isLogin(header.get("Cookie"))) {
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
