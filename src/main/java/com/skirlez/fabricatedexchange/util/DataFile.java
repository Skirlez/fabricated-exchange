package com.skirlez.fabricatedexchange.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;

import com.skirlez.fabricatedexchange.FabricatedExchange;

// This class represents a JSON file.
public class DataFile<T> {
    private final Type type;
    private final Path path;
    protected final String name;
    protected T value;
    public DataFile(Type type, String name) {
        this.type = type;
        this.name = name;
        this.path = ModConfig.CONFIG_DIR.resolve(name);
    }

    // Read the file from disk to update the instance.
    // if unsuccessful, set to the default value
    public void fetch() {
        if (Files.exists(path)) {
            try (BufferedReader reader = Files.newBufferedReader(path)) {
                value = ModConfig.GSON.fromJson(reader, type);
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
        if (!Files.exists(ModConfig.CONFIG_DIR)) {
            try {
                Files.createDirectory(ModConfig.CONFIG_DIR);
            }
            catch (Exception e) {
                FabricatedExchange.LOGGER.error("Could not create config directory for " + name + "!", e);
            }
        }
        if (value == null)
            return;
        try (BufferedWriter writer = Files.newBufferedWriter(path)) {
            ModConfig.GSON.toJson(value, writer);
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
        try (InputStream inputStream = FabricatedExchange.class.getClassLoader().getResourceAsStream("default_configs/" + name);
            Reader reader = new InputStreamReader(inputStream)) {
            value = ModConfig.GSON.fromJson(reader, type);
            process();
        } catch (Exception e) {
            FabricatedExchange.LOGGER.error(name + "'s default configuration could not be read from!", e);
            value = null;
        }
    }
    
    protected T getDefaultValue() {
        try (InputStream inputStream = FabricatedExchange.class.getClassLoader().getResourceAsStream("default_configs/" + name);
            Reader reader = new InputStreamReader(inputStream)) {
            return ModConfig.GSON.fromJson(reader, type);
        } catch (Exception e) {
            FabricatedExchange.LOGGER.error(name + "'s default configuration could not be read from!", e);
            return null;
        }
    }

    // This method will allow subclasses that override it to perform additional operations on the data that was fetched/set
    protected void process() {

    }

}
