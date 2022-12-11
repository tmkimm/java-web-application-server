package controller;

import db.DataBase;
import model.User;
import webserver.HttpRequest;
import webserver.HttpResponse;

import java.util.Map;

public class LoginController extends AbstractController{
    @Override
    public void service(HttpRequest request, HttpResponse response) {
        super.service(request, response);
    }

    @Override
    public void doPost(HttpRequest request, HttpResponse response) {
        Map<String, String> bodyMap = request.getBody();
        // 아이디가 존재하고 비밀번호가 일치하면
        User user = DataBase.findUserById(bodyMap.get("userId"));
        if(user != null && user.getPassword().equals(bodyMap.get("password"))) {
            response.sendRedirect("/index.html");
            response.addHeader("Set-Cookie", "logined=true");
        } else {
            response.forward("/user/login_failed.html");
        }
    }
}
