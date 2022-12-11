package webserver;

import java.io.*;
import java.lang.reflect.Array;
import java.net.Socket;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import controller.Controller;
import db.DataBase;
import model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.HttpRequestUtils;
import util.IOUtils;

public class RequestHandler extends Thread {
    private static final Logger log = LoggerFactory.getLogger(RequestHandler.class);

    private Socket connection;

    public RequestHandler(Socket connectionSocket) {
        this.connection = connectionSocket;
    }

    private String getDefaultPath(String path) {
        if(path.equals("/")) {
            return "/index.html";
        }
        return path;
    }
    public void run() {
        try (InputStream in = connection.getInputStream(); OutputStream out = connection.getOutputStream()) {
            HttpRequest request = new HttpRequest(in);
            HttpResponse response = new HttpResponse(out);
            Map<String, String> header = request.getHeaders();

            String url = request.getPath();
            Controller controller = RequestMapping.getController(request.getPath());
            if(controller == null) {
                String path = getDefaultPath(request.getPath());
                response.forward(path);
            } else {
                controller.service(request, response);
            }

//            log.debug("New Request URL: {}", url);
//            if(request.getMethod() == HttpMethod.POST && url.startsWith("/user/create")) { // POST 방식 회원가입
//                User user = new User(
//                        request.getParameter("userId")
//                        , request.getParameter("password")
//                        , request.getParameter("name")
//                        , request.getParameter("email"));
//                DataBase.addUser(user);
//                response.sendRedirect("/index.html");
//            }
//            else if(request.getMethod() == HttpMethod.POST && url.startsWith("/user/login")) { // 로그인
//                // 아이디가 존재하고 비밀번호가 일치하면
//                User user = DataBase.findUserById(request.getParameter("userId"));
//                if(user != null && user.getPassword().equals(request.getParameter("password"))) {
//                    response.sendRedirect("/index.html");
//                    response.addHeader("Set-Cookie", "logined=true");
//                } else {
//                    response.forward("/user/login_failed.html");
//                }
//            }
//            else if(url.startsWith("/user/list.html")) {    // 사용자 리스트
//                Map<String, String> cookies = HttpRequestUtils.parseCookies(header.get("Cookie"));
//                if(cookies.containsKey("logined") && Boolean.parseBoolean(cookies.get("logined"))) {
//                    Collection<User> users =  DataBase.findAll();
//                    response.forwardBody(users.toString());
//                } else {
//                    response.forward("/user/login.html");
//                }
//            }
//            else {
//                response.forward(url);
//            }
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }
}
