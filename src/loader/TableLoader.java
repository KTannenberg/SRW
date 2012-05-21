package loader;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import org.yaml.snakeyaml.Yaml;


public class TableLoader {

	private String tablePath;

	public TableLoader(String tablePath) {
		this.tablePath = tablePath;
	}
	
	public Table findTable(String tableName) throws FileNotFoundException, IOException {
		InputStream stream = null;
		try {
			stream = new FileInputStream(tablePath + "\\" + tableName);
		} catch (FileNotFoundException ex) {
			throw new FileNotFoundException("File with table " + tableName + " not found");
        }
        Yaml yaml = new Yaml();
		Table table;
		try {
			table = (Table) yaml.load(stream);
		} catch (Exception ex) {
			throw new IOException("Error while reading file " + tableName);
		}
		return table;
	}
}
