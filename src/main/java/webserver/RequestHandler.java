package webserver;

import java.io.*;
import java.lang.reflect.Array;
import java.net.Socket;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import com.sun.org.apache.xpath.internal.operations.Bool;
import db.DataBase;
import jdk.internal.util.xml.impl.Input;
import model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import repository.MemoryUserRepository;
import util.HttpRequestUtils;
import util.IOUtils;

import javax.xml.crypto.Data;

public class RequestHandler extends Thread {
    private static final Logger log = LoggerFactory.getLogger(RequestHandler.class);

    private Socket connection;

    public RequestHandler(Socket connectionSocket) {
        this.connection = connectionSocket;
    }

    public void run() {
        try (InputStream in = connection.getInputStream(); OutputStream out = connection.getOutputStream()) {
            HashMap<String, String> requests = toHttpRequestHashMap(in);

            String url = requests.get("URL");
            byte[] body;
            log.debug("New Request : {}", url);
            if(url.startsWith("POST /user/create")) { // POST 방식 회원가입
                Map<String, String> bodyMap = HttpRequestUtils.parseQueryString(requests.get("Body"));
                User user = new User(bodyMap.get("userId"), bodyMap.get("password"), bodyMap.get("name"), bodyMap.get("email"));
                DataBase.addUser(user);
                DataOutputStream dos = new DataOutputStream(out);
                response302Header(dos);
            }
            else if(url.startsWith("POST /user/login")) { // 로그인
                Map<String, String> bodyMap = HttpRequestUtils.parseQueryString(requests.get("Body"));
                // 아이디가 존재하고 비밀번호가 일치하면
                User user = DataBase.findUserById(bodyMap.get("userId"));
                if(user != null && user.getPassword().equals(bodyMap.get("password"))) {
                    DataOutputStream dos = new DataOutputStream(out);
                    response302LoginSuccessHeader(dos);
                } else {
                    responseResource(out, "/user/login_failed.html");
                }
            }
            else if(url.startsWith("GET /user/list.html")) {    // 사용자 리스트
                Map<String, String> cookies = HttpRequestUtils.parseCookies(requests.get("Cookie"));
                if(cookies.containsKey("logined") && Boolean.parseBoolean(cookies.get("logined"))) {
                    DataOutputStream dos = new DataOutputStream(out);
                    Collection<User> users =  DataBase.findAll();
                    body = users.toString().getBytes();
                    response200Header(dos, body.length, "text/html");
                    responseBody(dos, body);
                } else {
                    responseResource(out, "/user/login.html");
                }
            }
            else if(url.indexOf(".html") != -1 || url.indexOf(".css") != -1) {
                responseResource(out, parseUrl(url));
            } else {
                DataOutputStream dos = new DataOutputStream(out);
                body = "Hello World".getBytes();
                response200Header(dos, body.length, "text/html");
                responseBody(dos, body);
            }
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    public HashMap<String, String> toHttpRequestHashMap(InputStream in) {
        HashMap<String, String> requests = new HashMap<>();
        String line;
        try {
            BufferedReader br = new BufferedReader((new InputStreamReader(in)));
            // URL Setting
            line = br.readLine();
            requests.put("URL", line);

            // HTTP Headers Setting
            while(true) {
                line = br.readLine();
                if(line == null || line.isEmpty())
                    break;
                String[] keyValue = line.split(": ");
                requests.put(keyValue[0], keyValue[1]);
            }

            // HTTP Body Setting
            if(requests.get("URL").startsWith("POST")) {
                String requestBody = IOUtils.readData(br, Integer.parseInt(requests.get("Content-Length")));
                requests.put("Body", requestBody);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return requests;
    }
    public String parseUrl(String text) {
        int firstBlank = text.indexOf(" ") + 1;
        String url = text.substring(firstBlank, text.indexOf(" ", firstBlank));
        return url;
    }
    // 로그인 성공 response
    private void response302Header(DataOutputStream dos) {
        try {
            dos.writeBytes("HTTP/1.1 302 OK \r\n");
            dos.writeBytes("Location: /index.html \r\n");
            dos.writeBytes("Set-Cookie: logined=true; \r\n");    // Set Cookie
            dos.writeBytes("\r\n");
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }
    private void response302LoginSuccessHeader(DataOutputStream dos) {
        try {
            dos.writeBytes("HTTP/1.1 302 OK \r\n");
            dos.writeBytes("Location: /index.html \r\n");
            dos.writeBytes("Set-Cookie: logined=true; \r\n");    // Set Cookie
            dos.writeBytes("\r\n");
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    // Resource response
    private void responseResource(OutputStream out, String url) {
        try {
            DataOutputStream dos = new DataOutputStream(out);
            if(url.indexOf(".css") != -1) {
                log.debug("css file path : {}", "./webapp"+url);
            }
            byte[] body = Files.readAllBytes(new File("./webapp"+ url).toPath());
            response200Header(dos, body.length, url.indexOf(".css") != -1 ? "text/css" : "text/html");
            responseBody(dos, body);
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    private void response200Header(DataOutputStream dos, int lengthOfBodyContent, String contentType) {
        try {
            dos.writeBytes("HTTP/1.1 200 OK \r\n");
            dos.writeBytes("Content-Type: " + contentType + ";charset=utf-8\r\n");
            dos.writeBytes("Content-Length: " + lengthOfBodyContent + "\r\n");
            dos.writeBytes("\r\n");
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    private void responseBody(DataOutputStream dos, byte[] body) {
        try {
            dos.write(body, 0, body.length);
            dos.flush();
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }
}
