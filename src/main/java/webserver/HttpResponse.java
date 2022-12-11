package webserver;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;

// 응답 데이터를 갖고있는 클래스
public class HttpResponse {
    private static final Logger log = LoggerFactory.getLogger(RequestHandler.class);
    DataOutputStream dos;

    public HttpResponse(OutputStream out) {
        dos = new DataOutputStream(out);
    }

    // 헤더 추가
    public void addHeader(String key, String value) {
        try {
            dos.writeBytes(key + ": " + value + "; \r\n");
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    // HTML, CSS, JS 파일 읽기
    public void forward(String url) {
        try {
            byte[] body = Files.readAllBytes(new File("./webapp" + url).toPath());
            response200Header(dos, body.length, url.indexOf(".css") != -1 ? "text/css" : "text/html");
            responseBody(dos, body);
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    public void forwardBody(String body) {
        byte[] data = body.getBytes();
        response200Header(dos, data.length, "text/html");
        responseBody(dos, data);
    }

    // 다른 페이지로 이동
    public void sendRedirect(String url) {
        try {
            dos.writeBytes("HTTP/1.1 302 OK \r\n");
            dos.writeBytes("Location: " + url + " \r\n");
            dos.writeBytes("\r\n");
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    public void response200Header(DataOutputStream dos, int lengthOfBodyContent, String contentType) {
        try {
            dos.writeBytes("HTTP/1.1 200 OK \r\n");
            dos.writeBytes("Content-Type: " + contentType + ";charset=utf-8\r\n");
            dos.writeBytes("Content-Length: " + lengthOfBodyContent + "\r\n");
            dos.writeBytes("\r\n");
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    public void responseBody(DataOutputStream dos, byte[] body) {
        try {
            dos.write(body, 0, body.length);
            dos.flush();
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }
}
