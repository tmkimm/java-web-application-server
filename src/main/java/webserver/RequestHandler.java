package webserver;

import java.io.*;
import java.lang.reflect.Array;
import java.net.Socket;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

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
            String contentType = "text/html";
            String url = requests.get("URL");
            byte[] body;

            if(url.startsWith("GET /user/create")) {   // GET 방식 회원가입
                body = "create success!".getBytes();
                HashMap<String, String> queryParams = getQueryParams(url);  // 쿼리 파라미터 파싱
                User user = new User(queryParams.get("userId"), queryParams.get("password"), queryParams.get("name"), queryParams.get("email"));
                store.put(queryParams.get("userId"), user);
            }
            else if(url.startsWith("POST /user/create")) { // POST 방식 회원가입
                body = "create success!".getBytes();
                Map<String, String> bodyMap = HttpRequestUtils.parseQueryString(requests.get("Body"));
                User user = new User(bodyMap.get("userId"), bodyMap.get("password"), bodyMap.get("name"), bodyMap.get("email"));
                store.put(bodyMap.get("userId"), user);
            }
            else if(url.startsWith("GET /user/login")) { // 로그인
                body = "login page".getBytes();
                log.debug(store.toString());
            }
            else if(url.indexOf(".html") != -1) {
                body = Files.readAllBytes(new File("./webapp"+ parseUrl(url)).toPath());
            } else if(url.indexOf(".css") != -1) {
                body = Files.readAllBytes(new File("./webapp"+ parseUrl(url)).toPath());
                contentType = "text/css";
            } else {
                body = "Hello World".getBytes();
            }
            response200Header(dos, body.length, contentType);
            responseBody(dos, body);
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
    public HashMap<String, String> getQueryParams(String url) {
        if(url.indexOf("?") == -1) {
            return null;
        }
        HashMap<String, String> queryParams = new HashMap<>();
        String[] splitArr = url.substring(url.indexOf("?") + 1).split("&");
        String[] item;
        for(String v: splitArr) {
            item = v.split("=");
            if(item.length >= 1)
                queryParams.put(item[0], item[1]);
        }
        return queryParams;
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
