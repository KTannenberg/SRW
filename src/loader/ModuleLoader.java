package loader;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

public class ModuleLoader extends ClassLoader {

    private String binPath;

    public ModuleLoader(String binPath, ClassLoader parent) {
        super(parent);
        this.binPath = binPath;
    }

    @Override
    public Class<?> findClass(String className) throws ClassNotFoundException {
        try {
            /**
             * getting b-code from file and loading to runtime class
             */
            byte b[] = fetchClassFromFS(binPath + className + ".class");
            Class<?> result = defineClass(className, b, 0, b.length);
            return result;
        } catch (FileNotFoundException ex) {
            return super.findClass(className);
        } catch (IOException ex) {
            return super.findClass(className);
        }
    }

    private byte[] fetchClassFromFS(String path) throws FileNotFoundException,
            IOException {
        InputStream is = new FileInputStream(new File(path));

        long length = new File(path).length();
        if (length > Integer.MAX_VALUE) {
            throw new IOException("File is to large " + path);
        }

        // Create the byte array to hold the data
        byte[] bytes = new byte[(int) length];

        // Read in the bytes
        int offset = 0;
        int numRead = 0;
        while (offset < bytes.length
                && (numRead = is.read(bytes, offset, bytes.length - offset)) >= 0) {
            offset += numRead;
        }

        if (offset < bytes.length) {
            throw new IOException("Could not completely read file " + path);
        }

        is.close();
        return bytes;
    }
}