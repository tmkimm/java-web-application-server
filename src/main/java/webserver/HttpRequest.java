package webserver;

import util.HttpRequestUtils;
import util.IOUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

// 요청 데이터를 담고있는 클래스
public class HttpRequest {
    Map<String, String> headers;
    Map<String, String> queryParmas;
    Map<String, String> body;

    public Map<String, String> getHeaders() {
        return headers;
    }

    public Map<String, String> getQueryParmas() {
        return queryParmas;
    }

    public Map<String, String> getBody() {
        return body;
    }

    public HttpRequest(InputStream in) {
        headers = new HashMap<>();
        queryParmas = new HashMap<>();
        body = new HashMap<>();
        try {
            BufferedReader br = new BufferedReader((new InputStreamReader(in)));

            // URL Setting
            String line = br.readLine();
            headers.put("URL", line);

            // HTTP Headers Setting
            while (true) {
                line = br.readLine();
                if (line == null || line.isEmpty())
                    break;
                String[] keyValue = line.split(": ");
                headers.put(keyValue[0], keyValue[1]);
            }

            // HTTP QueryParmas
            if (headers.get("URL").startsWith("GET") && headers.get("URL").indexOf("?") != -1) {
                queryParmas = HttpRequestUtils.parseQueryString(headers.get("URL"));
            }

            // HTTP Body Setting
            if (headers.get("URL").startsWith("POST")) {
                String requestBody = IOUtils.readData(br, Integer.parseInt(headers.get("Content-Length")));
                body = HttpRequestUtils.parseQueryString(requestBody);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
