package webserver;

import java.io.*;
import java.lang.reflect.Array;
import java.net.Socket;
import java.nio.file.Files;
import java.util.ArrayList;

import jdk.internal.util.xml.impl.Input;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.IOUtils;

public class RequestHandler extends Thread {
    private static final Logger log = LoggerFactory.getLogger(RequestHandler.class);

    private Socket connection;

    public RequestHandler(Socket connectionSocket) {
        this.connection = connectionSocket;
    }

    public void run() {
        try (InputStream in = connection.getInputStream(); OutputStream out = connection.getOutputStream()) {
            ArrayList<String> requests = toHttpRequest(in);
            String url = findUrl(requests.get(0));

            // 컨트롤러로 refactoring
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
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }
    public ArrayList<String> toHttpRequest(InputStream in) {
        ArrayList<String> requests = new ArrayList<String>();
        try {
            BufferedReader br = new BufferedReader((new InputStreamReader(in)));
            String line;
            while(true) {
                line = br.readLine();
                log.debug(line);
                if((line == null || line.isEmpty()))
                    break;
                requests.add(line);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        log.debug("New Request >>  Url : {}", requests.get(0));
        return requests;
    }
    public String findUrl(String text) {
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

    private void responseBody(DataOutputStream dos, byte[] body) {
        try {
            dos.write(body, 0, body.length);
            dos.flush();
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }
}
