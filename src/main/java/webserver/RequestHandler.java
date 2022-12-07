package webserver;

import java.io.*;
import java.net.Socket;
import java.nio.file.Files;
import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RequestHandler extends Thread {
    private static final Logger log = LoggerFactory.getLogger(RequestHandler.class);

    private Socket connection;

    public RequestHandler(Socket connectionSocket) {
        this.connection = connectionSocket;
    }

    public void run() {

        try (InputStream in = connection.getInputStream(); OutputStream out = connection.getOutputStream()) {
            BufferedReader br = new BufferedReader((new InputStreamReader(in)));
            String line;
            ArrayList<String> requests = new ArrayList<String>();
            while(true) {
                line = br.readLine();
                if(line == null || line.isEmpty())
                    break;
                requests.add(line);
            }
            log.debug("New Client Request! Url : {}", requests.get(0));
            int firstBlank = requests.get(0).indexOf(" ") + 1;
            String url = requests.get(0).substring(firstBlank, requests.get(0).indexOf(" ", firstBlank));

            DataOutputStream dos = new DataOutputStream(out);
            String contentType = "text/html";
            byte[] body;
            if(url.indexOf(".html") != -1) {
                body = Files.readAllBytes(new File("./webapp"+ url).toPath());
            } else if(url.indexOf(".css") != -1) {
                body = Files.readAllBytes(new File("./webapp"+ url).toPath());
                contentType = "text/css";
            }else {
                body = "Hello World".getBytes();
            }
            response200Header(dos, body.length, contentType);
            responseBody(dos, body);
            // InputStream 첫라인 읽어서 컨트롤러 만들기
            // favicon.ico 제외

            // 서빙하도록
            // GET /index.html?id=1234 HTTP/1.1
            // GET / HTTP/1.1

            // 첫 스페이스 찾아서 GET
            // /뒤에 문자열 잘라서 경로

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
