package controller;

import db.DataBase;
import model.User;
import session.HttpSession;
import session.HttpSessions;
import webserver.HttpRequest;
import webserver.HttpResponse;

import java.util.Map;

public class LoginController implements Controller{
    public void service(HttpRequest request, HttpResponse response) {
        // 아이디가 존재하고 비밀번호가 일치하면
        User user = DataBase.findUserById(request.getParameter("userId"));
        if(user != null && user.getPassword().equals(request.getParameter("password"))) {
            HttpSession session = request.getSession();
            session.setAttributes("user", user);
            session.setAttributes("isLogin", true);     // 로그인 여부
            response.sendRedirect("/index.html");
        } else {
            response.forward("/user/login_failed.html");
        }
    }
}
