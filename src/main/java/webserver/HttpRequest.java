package webserver;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
    private HttpMethod method;
    private String path;
    private Map<String, String> params = new HashMap<>();
    private Map<String, String> headers = new HashMap<>();

    public Map<String, String> getHeaders() {
        return headers;
    }

    public HttpMethod getMethod() {
        return method;
    }

    public String getPath() {
        return path;
    }

    public Map<String, String> getParams() {
        return params;
    }

    public String getParameter(String key) {
        return params.get(key);
    }

    public HttpRequest(InputStream in) {
        final Logger log = LoggerFactory.getLogger(RequestHandler.class);
        try {
            BufferedReader br = new BufferedReader((new InputStreamReader(in)));

            String line = br.readLine();
            processRequestLine(line);   // Set request inf

            // HTTP Headers Setting
            while (true) {
                line = br.readLine();
                if (line == null || line.isEmpty())
                    break;
                String[] keyValue = line.split(": ");
                headers.put(keyValue[0], keyValue[1]);
            }

            // HTTP Body Setting
            if (getMethod() == HttpMethod.POST) {
                String requestBody = IOUtils.readData(br, Integer.parseInt(headers.get("Content-Length")));
                params = HttpRequestUtils.parseQueryString(requestBody);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    private void processRequestLine(String line) {
        String[] tokens = line.split(" ");
        method = HttpMethod.valueOf(tokens[0]);

        if(method == HttpMethod.POST) {
            path = tokens[1];
            return;
        }
        int index = tokens[1].indexOf("?");
        if(index == -1) {
            path = tokens[1];
        } else {
            path = tokens[1].substring(0, index);
            params = HttpRequestUtils.parseQueryString(tokens[1].substring(index + 1));
        }
    }
}
