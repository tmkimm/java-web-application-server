package webserver;

import util.HttpRequestUtils;

import java.util.HashMap;
import java.util.Map;

public class HttpCookie {
    private Map<String, String> cookies;

    public HttpCookie(String requestCookie) {
        cookies = HttpRequestUtils.parseCookies(requestCookie);
    }
    public String getCookie(String key) {  // 없는 key에 접근하면 어떻게 될까?
        return cookies.get(key);
    }
}
