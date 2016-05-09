package test;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

public class Tests {

	public static void main(String[] args) {
		System.out.println(getPublicStaticMethods(Test.class, "main"));
	}

	public static List<Method> getPublicStaticMethods(final Class<?> clazz, final String name) {
		final List<Method> methods = new ArrayList<>();
		for (final Method method : clazz.getDeclaredMethods()) {
			if (!Modifier.isPublic(method.getModifiers()) || !Modifier.isStatic(method.getModifiers())) {
				// skip non-static methods
				continue;
			}

			if (method.getName().equals(name)) {
				methods.add(method);
			}
		}
		return methods;
	}
}