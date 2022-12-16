package session;

import com.google.common.collect.Maps;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class HttpSessions {
    private static Map<String, HttpSession> sessions = Maps.newHashMap();

    public static HttpSession getSession(String id) {
        HttpSession session = sessions.get(id);

        if(session == null && sessions.containsKey(id) == false) {
            session = new HttpSession(id);
            sessions.put(id, session);
            return session;
        }
        return session;
    }

    public static void remove(String id) {
        sessions.remove(id);
    }
}
