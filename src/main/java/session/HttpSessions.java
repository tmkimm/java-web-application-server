package session;

import com.google.common.collect.Maps;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class HttpSessions {
    private static Map<UUID, HttpSession> sessions = Maps.newHashMap();

    public static HttpSession getSession(UUID id) {
        return sessions.get(id);
    }

    public static void addSession(HttpSession session) {
        sessions.put(session.getId(), session);
    }

    public static void removeSession(UUID key) {
        sessions.remove(key);
    }

    public static void clearSession() {
        sessions.clear();
    }
}
