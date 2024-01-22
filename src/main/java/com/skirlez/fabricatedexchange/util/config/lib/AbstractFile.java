package com.skirlez.fabricatedexchange.util.config.lib;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Collectors;

import com.skirlez.fabricatedexchange.FabricatedExchange;
import com.skirlez.fabricatedexchange.util.config.ModDataFiles;

/** This class represents a file with a value that can be saved to permanent storage.
 * Files are first fetched, then getValue() can be called and modified, then setValue() and save().
 * This abstract class only leaves readValue and writeValue to be implemented by subclasses, and optionally process().
 * 
 * Would use Optional when returning values in case reading failed instead of null, but since it reads the default value as a failsafe 
 * it should only return null in case that fails too, and I don't believe it's possible unless there's something wrong with the file itself. */
public abstract class AbstractFile<T> {
	
	// The folder where the file is stored
	private final Path path;
	// The type of the value
	protected final Type type;
	// The name of the file
	protected final String name;
	// The file's value
	protected T value;
	// The file's actual character information.
	protected String file;
	
	public AbstractFile(Type type, String name) {
		this.type = type;
		this.name = name;
		this.path = ModDataFiles.CONFIG_DIR.resolve(name);
	}


	// Subclasses must override these methods to save the file
	protected abstract T readValue(Reader reader) throws Exception;
	protected abstract void writeValue(Writer writer, T value) throws Exception;

	/** Read the file from disk to update the instance.
 	 * if unsuccessful, set to the default value */
	public void fetch() {
		if (Files.exists(path)) {
			try (Reader reader = Files.newBufferedReader(path)) {
				file = readAsString(reader);
				value = process(readValue(new StringReader(file)));
				constProcess();
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
	
	/** Gets a copy of the value of the file as it was when last fetched or saved. */
	public T getCopy() {
		try {
			return process(readValue(new StringReader(file)));
		}
		catch (Exception e) { 
			FabricatedExchange.LOGGER.error(name + " could not be parsed!", e);
		}
		return value;
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
		try (BufferedWriter writer = Files.newBufferedWriter(path);
			StringWriter stringWriter = new StringWriter()) {
			writeValue(writer, value);
			writeValue(stringWriter, value);
			file = stringWriter.toString();
		} 
		catch (Exception e) {
			FabricatedExchange.LOGGER.error(name + " could not be saved!", e);
		}
	}

	public void setValue(T newValue) {
		value = newValue;
		constProcess();
	}

	public void setValueAndSave(T newValue) {
		setValue(newValue);
		save();
	}

	public void setValueToDefault() {
		try (InputStream inputStream = FabricatedExchange.class.getClassLoader().getResourceAsStream("fe_default_configs/" + name);
			Reader reader = new InputStreamReader(inputStream)) {
			file = readAsString(reader);
			value = process(readValue(reader));
			constProcess();
		} catch (Exception e) {
			FabricatedExchange.LOGGER.error(name + "'s default configuration could not be read from!", e);
			value = null;
		}
	}
	
	protected T getDefaultValue() {
		try (InputStream inputStream = FabricatedExchange.class.getClassLoader().getResourceAsStream("fe_default_configs/" + name);
			Reader reader = new InputStreamReader(inputStream)) {
			return process(readValue(reader));
		} catch (Exception e) {
			FabricatedExchange.LOGGER.error(name + "'s default configuration could not be read from!", e);
			return null;
		}
	}

    public static String readAsString(Reader reader) {
    	BufferedReader bufferedReader = new BufferedReader(reader);
        return bufferedReader.lines().collect(Collectors.joining(System.lineSeparator()));
    }
	
	/** This method will allow subclasses that override it to perform additional operations on the data that was fetched/set 
	 * The value given is NOT equal to this.value when this is called, this.value still holds the previous value. */
	protected T process(T value) {
		return value;
	}
	
	/** This method will allow subclasses that override it to read from this.value and then do whatever (modify other class members etc..)  */
	protected void constProcess() {
		
	}
}
