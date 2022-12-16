package controller;

import db.DataBase;
import model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import session.HttpSession;
import session.HttpSessions;
import util.HttpRequestUtils;
import webserver.HttpRequest;
import webserver.HttpResponse;
import webserver.RequestHandler;

import java.util.Collection;
import java.util.Map;
import java.util.UUID;

public class ListUserController implements Controller{
    private static final Logger log = LoggerFactory.getLogger(RequestHandler.class);
    public void service(HttpRequest request, HttpResponse response) {

        if(isLogin(request.getSession())) {
            Collection<User> users =  DataBase.findAll();
            response.forwardBody(users.toString());
        } else {
            response.sendRedirect("/user/login.html");
        }
    }

    public boolean isLogin(HttpSession session) {
        Object user = session.getAttributes("user");
        if(user == null)
            return false;

        return true;
    }
}
