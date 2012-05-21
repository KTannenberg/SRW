package runtime;

import java.util.Deque;
import java.util.Map;

public class Channel {
	Map<Integer, Object> data;
	Deque<String> event;
}
