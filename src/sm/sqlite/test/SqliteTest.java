package sm.sqlite.test;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Set;

//import org.reflections.Reflections;
//import javassist.util.proxy.*;

public class SqliteTest {
	/*public static void main(String[] args) throws Exception {
		System.out.println("Testing SQLiteAnnotation stuff");
		
		Reflections reflections = new Reflections("sm.sqlite");
		Set<Class<?>> annotated = reflections.getTypesAnnotatedWith(SqliteClass.class);
		
		for(Class<?> clazz : annotated) {
			System.out.println("    Url: " + clazz);
		}
		
		test();
	}
	
	public static void test() {
		RigidBodyBoundsTest object = request(RigidBodyBoundsTest.class);
		System.out.println(object.getMaxX());
		System.out.println(object.getMinY());
		System.out.println(object);
		object.getSomething();
	}
	
	@SuppressWarnings("unchecked")
	public static <T> T request(final Class<T> interfaceClass) {
		try {
			ProxyFactory proxy = new ProxyFactory();
			proxy.setInterfaces(new Class<?>[] { interfaceClass });
			proxy.setFilter(new MethodFilter() {
				public boolean isHandled(Method m) {
					return !m.getName().equals("finalize");
				}
			});
			Class<?> clazz = proxy.createClass();
			MethodHandler mi = new MethodHandler() {
				@Override
				public Object invoke(Object self, Method thisMethod, Method proceed, Object[] args) throws Throwable {
					if(thisMethod.getName().equals("toString")) return clazz.getName() + '@' + Integer.toHexString(proxy.hashCode());
					if(!thisMethod.isAnnotationPresent(SqliteCommand.class)) return null;
					// TODO: SQL Command
					
					Class<?> returnType = thisMethod.getReturnType();
					System.out.println("Called: " + thisMethod);
					
					Annotation command = thisMethod.getAnnotation(SqliteCommand.class);
					System.out.println("Return: " + returnType);
					System.out.println("Command: " + command);
					System.out.println("Return: " + thisMethod.isAnnotationPresent(SqliteCommand.class));
					System.out.println();
					
					return 0.0;
				}
			};
			Object result = clazz.newInstance();
			((Proxy)result).setHandler(mi);
			return (T)result;
		} catch(Exception e) {
			e.printStackTrace();
		}
		return null;
	}*/
}
