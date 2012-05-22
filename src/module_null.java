

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import loader.Module;

public class module_null implements Module {
    private static final Map<Integer, String> code;
    private static final Set<String> data;

    static {
        code = new HashMap<Integer, String>();
        code.put(0, null);
        code.put(1, "zero");
        data = new LinkedHashSet<String>();
    }

    public int run(Map<String, Object> storage) {
        return 1;
    }

    public Map<Integer, String> code() {
        return code;
    }

    public Set<String> data() {
        return data;
    }
}
