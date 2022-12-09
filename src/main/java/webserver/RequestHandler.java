package webserver;

import java.io.*;
import java.lang.reflect.Array;
import java.net.Socket;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import com.sun.org.apache.xpath.internal.operations.Bool;
import jdk.internal.util.xml.impl.Input;
import model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import repository.MemoryUserRepository;
import util.HttpRequestUtils;
import util.IOUtils;

public class RequestHandler extends Thread {
    private static final Logger log = LoggerFactory.getLogger(RequestHandler.class);

    private Socket connection;
    private static HashMap<String, User> store = new HashMap<String, User>();

    public RequestHandler(Socket connectionSocket) {
        this.connection = connectionSocket;
    }

    public void run() {
        try (InputStream in = connection.getInputStream(); OutputStream out = connection.getOutputStream()) {
            HashMap<String, String> requests = toHttpRequestHashMap(in);

            DataOutputStream dos = new DataOutputStream(out);
            String url = requests.get("URL");
            byte[] body;
            log.debug("Request : {}", url);
            if(url.startsWith("POST /user/create")) { // POST 방식 회원가입
                Map<String, String> bodyMap = HttpRequestUtils.parseQueryString(requests.get("Body"));
                User user = new User(bodyMap.get("userId"), bodyMap.get("password"), bodyMap.get("name"), bodyMap.get("email"));
                store.put(bodyMap.get("userId"), user);
                response302(dos, "/index.html", false);
            }
            else if(url.startsWith("POST /user/login")) { // 로그인
                Map<String, String> bodyMap = HttpRequestUtils.parseQueryString(requests.get("Body"));
                // 아이디가 존재하고 비밀번호가 일치하면
                if(store.containsKey(bodyMap.get("userId")) && store.get(bodyMap.get("userId")).getPassword().equals(bodyMap.get("password"))) {
                    response302(dos, "/index.html", true);
                } else {
                    response302(dos, "/user/login_failed.html", false);
                }
            }
            else if(url.startsWith("GET /user/list.html")) {
                Map<String, String> cookies = HttpRequestUtils.parseCookies(requests.get("Cookie"));
                if(cookies.containsKey("logined") && Boolean.parseBoolean(cookies.get("logined"))) {
                    StringBuilder userList = new StringBuilder();
                    store.forEach((key, user)-> {
                        userList.append("<p>" + user.getUserId() + "</p><br>");
                    });
                    body = userList.toString().getBytes();
                    response200Header(dos, body.length, "text/html");
                    responseBody(dos, body);
                } else {
                    response302(dos, "/user/login.html", false);
                }
            }
            else if(url.indexOf(".html") != -1) {
                body = Files.readAllBytes(new File("./webapp"+ parseUrl(url)).toPath());
                response200Header(dos, body.length, "text/html");
                responseBody(dos, body);
            } else if(url.indexOf(".css") != -1) {
                body = Files.readAllBytes(new File("./webapp"+ parseUrl(url)).toPath());
                response200Header(dos, body.length, "text/css");
                responseBody(dos, body);
            } else {
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
    private void response302(DataOutputStream dos, String url, Boolean isCookie) {
        try {
            dos.writeBytes("HTTP/1.1 302 OK \r\n");
            dos.writeBytes("Location: " + url + "\r\n");
            if(isCookie)
                dos.writeBytes("Set-Cookie: logined=true; \r\n");    // Set Cookie
            dos.writeBytes("\r\n");
            dos.flush();
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
