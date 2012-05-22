package loader;

import java.util.Map;
import java.util.Set;

import runtime.Channel;

public interface Module {
    public int run(Map<String, Object> storage);

    public Map<Integer, String> code();

    public Set<String> data();
}
