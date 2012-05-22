

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import loader.Module;

public class module_sopln implements Module {
    private static final Map<Integer, String> code;
    private static final Set<String> data;

    static {
        code = new HashMap<Integer, String>();
        code.put(0, null);
        code.put(1, "digit");
        code.put(2, "alpha");
        code.put(-1, "MODULE: Empty storage.get(\"line\")");

        data = new LinkedHashSet<String>();
        data.add("line");
    }

    public int run(Map<String, Object> storage) {
        if (storage.get("line") == null) {
            return -1;
        } else {
            String line = (String) storage.get("line");
            System.out.println("MODULE >> " + line);
            if (line.matches("\\d+"))
                return 1;
            else if (line.matches("[A-Za-z]+"))
                return 2;
            else
                return 0;
        }
    }

    public Map<Integer, String> code() {
        return code;
    }

    public Set<String> data() {
        return data;
    }

}
