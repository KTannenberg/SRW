package loader;

import java.util.Map;
import java.util.Set;

public interface Module {
	public void load();
	public int run();
	public void unload();
	public Map<Integer, String> code();
	public Set<String> data();
}
