package controller;

import db.DataBase;
import model.User;
import webserver.HttpRequest;
import webserver.HttpResponse;

import java.util.Map;

public class CreateUserController extends AbstractController{
    @Override
    public void service(HttpRequest request, HttpResponse response) {
        super.service(request, response);
    }

    @Override
    public void doPost(HttpRequest request, HttpResponse response) {
        Map<String, String> bodyMap = request.getBody();
        User user = new User(bodyMap.get("userId"), bodyMap.get("password"), bodyMap.get("name"), bodyMap.get("email"));
        DataBase.addUser(user);
        response.sendRedirect("/index.html");
    }
}
