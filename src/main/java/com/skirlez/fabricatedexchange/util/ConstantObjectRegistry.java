package com.skirlez.fabricatedexchange.util;

import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/** This object can be used to register constant (final) objects with a String ID, for the purpose of syncing client and server side objects.
 * Will work for any constant object that exists on both. */
public class ConstantObjectRegistry {
	private static final ConcurrentHashMap<String, Object> registry = new ConcurrentHashMap<String, Object>();
	private static final ConcurrentHashMap<Object, String> evilReverseRegistry = new ConcurrentHashMap<Object, String>();

	public static Optional<String> getObjectId(Object object) {
		if (!evilReverseRegistry.containsKey(object))
			return Optional.empty();
		return Optional.of(evilReverseRegistry.get(object));
	}

	public static <T> T register(String key, T object) {
		registry.put(key, object);
		evilReverseRegistry.put(object, key);
		return object;
	}

	@SuppressWarnings("unchecked")
	public static <T> Optional<T> getObject(String key) {
		try {
			return Optional.of((T) registry.get(key));
		}
		catch (Exception e) {
			return Optional.empty();
		}
	}

}
