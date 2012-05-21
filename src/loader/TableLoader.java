package loader;

import java.io.FileNotFoundException;
import java.io.IOException;


public class TableLoader {

	private String tablePath;

	public TableLoader(String tablePath) {
		this.tablePath = tablePath;
	}
	
	public Table findTable(String tableName) throws FileNotFoundException, IOException {
		return null;
	}
}
