package org.jake.utils;

import java.lang.reflect.Array;
import java.util.LinkedList;
import java.util.List;

public class IterableUtils {
	
	
	public static <T> List<T> toList(Iterable<T> it) {
		if (it instanceof List) {
			return (List<T>) it;
		}
		List<T> result = new LinkedList<T>();
		for (T t : it) {
			result.add(t);
		}
		return result;
	}
	
	@SuppressWarnings("unchecked")
	public static <T> T[] asArray(Iterable<T> it, Class<T> clazz) {
		List<T> list = toList(it);
		return (T[]) Array.newInstance(clazz, list.size());
	}

}