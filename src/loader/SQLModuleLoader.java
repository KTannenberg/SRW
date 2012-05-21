package loader;


public class SQLModuleLoader extends ClassLoader {
	//TODO: implement it
	
	private String pathtobin;

	public SQLModuleLoader(String pathtobin, ClassLoader parent) {
		super(parent);
		this.pathtobin = pathtobin;
	}
	
	@Override
	public Class<?> findClass(String className) throws ClassNotFoundException {
		byte b[] = fetchClassFromSQL("DB name", "login", "password", "table", className);
		return defineClass(className, b, 0, b.length);
	}

	private byte[] fetchClassFromSQL(String string, String string2,
			String string3, String string4, String className) {
		// TODO Auto-generated method stub
		return null;
	}
}