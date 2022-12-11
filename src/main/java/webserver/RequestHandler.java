package webserver;

import java.io.*;
import java.lang.reflect.Array;
import java.net.Socket;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

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

    public void run() {
        try (InputStream in = connection.getInputStream(); OutputStream out = connection.getOutputStream()) {
            HttpRequest request = new HttpRequest(in);
            HttpResponse response = new HttpResponse(out);
            Map<String, String> header = request.getHeaders();
            String url = header.get("URL");
            log.debug("New Request : {}", url);
            if(url.startsWith("POST /user/create")) { // POST 방식 회원가입
                Map<String, String> bodyMap = request.getBody();
                User user = new User(bodyMap.get("userId"), bodyMap.get("password"), bodyMap.get("name"), bodyMap.get("email"));
                DataBase.addUser(user);
                response.sendRedirect("/index.html");
            }
            else if(url.startsWith("POST /user/login")) { // 로그인
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
            else if(url.startsWith("GET /user/list.html")) {    // 사용자 리스트
                Map<String, String> cookies = HttpRequestUtils.parseCookies(header.get("Cookie"));
                if(cookies.containsKey("logined") && Boolean.parseBoolean(cookies.get("logined"))) {
                    Collection<User> users =  DataBase.findAll();
                    response.forwardBody(users.toString());
                } else {
                    response.forward("/user/login.html");
                }
            }
            else if(url.indexOf(".html") != -1 || url.indexOf(".css") != -1) {
                response.forward(parseUrl(url));
            }
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    public String parseUrl(String text) {
        int firstBlank = text.indexOf(" ") + 1;
        String url = text.substring(firstBlank, text.indexOf(" ", firstBlank));
        return url;
    }
}
