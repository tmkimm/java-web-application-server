package session;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class HttpSession {
    private UUID id;
    private Map<String, Object> attributes = new HashMap<>();

    public HttpSession() {
        this.id = UUID.randomUUID();
    }

    public UUID getId() {
        return this.id;
    }

    public Object getAttributes(String key) {
        return this.attributes.get(key);
    }

    public void setAttributes(String key, Object value) {
        this.attributes.put(key, value);
    }

    public void removeAttribute(String key) {
        this.attributes.remove(key);
    }

    public void invalidate() {
        this.attributes.clear();
    }
}
