package com.skirlez.fabricatedexchange.util.config.lib;

import java.io.Reader;
import java.io.Writer;
import java.lang.reflect.Type;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializer;
import com.skirlez.fabricatedexchange.util.SuperNumber;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;

/** an implementation of AbstractFile with GSON */
public class DataFile<T> extends AbstractFile<T> {
	
	private static final Gson GSON = new GsonBuilder()
		.registerTypeAdapter(SuperNumber.class, (JsonSerializer<SuperNumber>)(superNumber, type, jsonSerializationContext) 
		-> new JsonPrimitive(superNumber.divisionString()))
		.registerTypeAdapter(SuperNumber.class, (JsonDeserializer<SuperNumber>) (jsonElement, type, jsonDeserializationContext) 
		-> new SuperNumber(jsonElement.getAsString()))
		
		.registerTypeAdapter(Item.class, (JsonSerializer<Item>)(item, type, jsonSerializationContext) 
		-> new JsonPrimitive(Registries.ITEM.getId(item).toString()))
		.registerTypeAdapter(Item.class, (JsonDeserializer<Item>) (jsonElement, type, jsonDeserializationContext) 
		-> Registries.ITEM.get(new Identifier(jsonElement.getAsString())))
		
		.registerTypeAdapter(Enchantment.class, (JsonSerializer<Enchantment>)(item, type, jsonSerializationContext) 
		-> new JsonPrimitive(Registries.ENCHANTMENT.getId(item).toString()))
		.registerTypeAdapter(Enchantment.class, (JsonDeserializer<Enchantment>) (jsonElement, type, jsonDeserializationContext) 
		-> Registries.ENCHANTMENT.get(new Identifier(jsonElement.getAsString())))
		
		.setLenient()
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
