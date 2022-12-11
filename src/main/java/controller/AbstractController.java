package controller;

import webserver.HttpRequest;
import webserver.HttpResponse;

public abstract class AbstractController implements Controller {
    @Override
    public void service(HttpRequest request, HttpResponse response) {

    }

    public void doGet(HttpRequest request, HttpResponse response) {

    }

    public void doPost(HttpRequest request, HttpResponse response) {

    }

}
