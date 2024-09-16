package com.skirlez.fabricatedexchange.util.config.lib;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.json.JsonReadFeature;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.core.util.DefaultIndenter;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;

/** an implementation of AbstractFile using Jackson */
public class DataFile<T> extends AbstractFile<T> {
	private static final ObjectMapper objectMapper = JsonMapper.builder(JsonFactory.builder()
			  .enable(JsonReadFeature.ALLOW_TRAILING_COMMA) // This right here is why I switched from GSON
			  .build()).build();
	
	private final TypeReference<T> typeReference;
	
	public DataFile(TypeReference<T> typeReference, String name) {
		super(name);
		this.typeReference = typeReference;

	}
	@Override
	protected T readValue(Reader reader) throws Exception {
		return objectMapper.readerFor(typeReference).readValue(reader);
	}
	@Override
	protected void writeValue(Writer writer, T value) throws Exception  {
		objectMapper.writerFor(typeReference).with(new ActuallyGoodPrettyPrinter()).writeValue(writer, value);
		//YAML.dump(value, writer);
	}
}

@SuppressWarnings("serial")
class ActuallyGoodPrettyPrinter extends DefaultPrettyPrinter {
	public ActuallyGoodPrettyPrinter() {
		_objectIndenter = new DefaultIndenter("   ", System.lineSeparator());
	}
	public void writeObjectFieldValueSeparator(JsonGenerator g) throws IOException {
		g.writeRaw(": ");
	}
	public void writeStartArray(JsonGenerator g) throws IOException {
		g.writeRaw("[");
		_nesting++;
		_objectIndenter.writeIndentation(g, _nesting);
	}
	public void writeEndArray(JsonGenerator g, int nrOfValues) throws IOException {
		_nesting--;
		_objectIndenter.writeIndentation(g, _nesting);
		g.writeRaw(']');
	}
	public void beforeArrayValues(JsonGenerator g) throws IOException {
	}
	@Override
	public void writeArrayValueSeparator(JsonGenerator g) throws IOException {
		g.writeRaw(',');
		_objectIndenter.writeIndentation(g, _nesting);
	}
	@Override
	public DefaultPrettyPrinter createInstance() {
		return new ActuallyGoodPrettyPrinter();
	}
}