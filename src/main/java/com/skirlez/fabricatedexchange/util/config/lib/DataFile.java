package com.skirlez.fabricatedexchange.util.config.lib;

import java.io.Reader;
import java.io.Writer;
import java.lang.reflect.Type;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
/** an implementation of AbstractFile with GSON */
public class DataFile<T> extends AbstractFile<T> {
	
	private static final Gson GSON = new GsonBuilder()
		.setPrettyPrinting() // we do want these to be editable by users
		.create();


	
	public DataFile(Type type, String name) {
		super(type, name);
	}

	@Override
	protected T readValue(Reader reader) throws Exception {
		return GSON.fromJson(reader, type);
	}
	@Override
	protected void writeValue(Writer writer, T value) throws Exception  {
		GSON.toJson(value, writer);
	}




}
