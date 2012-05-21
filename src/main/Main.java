package main;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

import loader.Table;

import org.yaml.snakeyaml.Yaml;

public class Main {

    /**
     * @param args
     */
    public static void main(String[] args) {
        Yaml yaml = new Yaml();
        InputStream stream = null;
        try {
            stream = new FileInputStream("test\\main.table");
        } catch (FileNotFoundException ex) {
        }

        Table table = (Table) yaml.load(stream);
        System.out.println(yaml.dump(table));

    }
}
