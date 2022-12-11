package controller;

import db.DataBase;
import model.User;
import webserver.HttpRequest;
import webserver.HttpResponse;

import java.util.Map;

public class LoginController implements Controller{
    public void service(HttpRequest request, HttpResponse response) {
        // 아이디가 존재하고 비밀번호가 일치하면
        User user = DataBase.findUserById(request.getParameter("userId"));
        if(user != null && user.getPassword().equals(request.getParameter("password"))) {
            response.sendRedirect("/index.html");
            response.addHeader("Set-Cookie", "logined=true");
        } else {
            response.forward("/user/login_failed.html");
        }
    }
}
