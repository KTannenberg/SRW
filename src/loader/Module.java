package loader;

import java.util.Map;
import java.util.Set;

import runtime.Channel;

public interface Module {
    public int run(Channel channel);

    public Map<Integer, String> code();

    public Set<String> data();
}
