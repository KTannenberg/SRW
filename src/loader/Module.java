package loader;

import java.util.Map;
import java.util.Set;

public interface Module {
    public int run();

    public Map<Integer, String> code();

    public Set<String> data();
}
