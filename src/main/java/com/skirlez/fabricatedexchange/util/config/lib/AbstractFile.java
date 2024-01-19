package com.skirlez.fabricatedexchange.util.config.lib;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.Writer;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;

import com.skirlez.fabricatedexchange.FabricatedExchange;
import com.skirlez.fabricatedexchange.util.config.ModDataFiles;

/** This class represents a file with a value that can be saved to permanent storage. */
public abstract class AbstractFile<T> {
	
	// The folder where the file is stored
	private final Path path;
	// The type of the value
	protected final Type type;
	// The name of the file
	protected final String name;
	// The file's value
	protected T value;
	
	public AbstractFile(Type type, String name) {
		this.type = type;
		this.name = name;
		this.path = ModDataFiles.CONFIG_DIR.resolve(name);
	}


	// Subclasses must override these methods to save the file
	protected abstract T readValue(Reader reader) throws Exception;
	protected abstract void writeValue(Writer writer, T value) throws Exception;

	// Read the file from disk to update the instance.
	// if unsuccessful, set to the default value
	public void fetch() {
		if (Files.exists(path)) {
			try (BufferedReader reader = Files.newBufferedReader(path)) {
				value = readValue(reader);
				process();
			} 
			catch (Exception e) {
				FabricatedExchange.LOGGER.error(name + " exists but could not be read!", e);
				setValueToDefault();
			}
		}
		else {
			FabricatedExchange.LOGGER.info("Data file " + name + " doesn't exist, creating it...");
			setValueToDefault();
			save();
		}
	}

	public T getValue() {
		return value;
	}

	public T fetchAndGetValue() {
		fetch();
		return getValue();
	}
	
	// Write the instance's current data to disk
	public void save() {
		if (!Files.exists(ModDataFiles.CONFIG_DIR)) {
			try {
				Files.createDirectory(ModDataFiles.CONFIG_DIR);
			}
			catch (Exception e) {
				FabricatedExchange.LOGGER.error("Could not create config directory for " + name + "!", e);
			}
		}
		if (value == null)
			return;
		try (BufferedWriter writer = Files.newBufferedWriter(path)) {
			writeValue(writer, value);
		} 
		catch (Exception e) {
			FabricatedExchange.LOGGER.error(name + " could not be saved!", e);
		}
	}

	public void setValue(T newValue) {
		value = newValue;
		process();
	}

	public void setValueAndSave(T newValue) {
		setValue(newValue);
		save();
	}

	public void setValueToDefault() {
		try (InputStream inputStream = FabricatedExchange.class.getClassLoader().getResourceAsStream("fe_default_configs/" + name);
			Reader reader = new InputStreamReader(inputStream)) {
			value = readValue(reader);
			process();
		} catch (Exception e) {
			FabricatedExchange.LOGGER.error(name + "'s default configuration could not be read from!", e);
			value = null;
		}
	}
	
	protected T getDefaultValue() {
		try (InputStream inputStream = FabricatedExchange.class.getClassLoader().getResourceAsStream("fe_default_configs/" + name);
			Reader reader = new InputStreamReader(inputStream)) {
			return readValue(reader);
		} catch (Exception e) {
			FabricatedExchange.LOGGER.error(name + "'s default configuration could not be read from!", e);
			return null;
		}
	}

	// This method will allow subclasses that override it to perform additional operations on the data that was fetched/set
	protected void process() {

	}

}
