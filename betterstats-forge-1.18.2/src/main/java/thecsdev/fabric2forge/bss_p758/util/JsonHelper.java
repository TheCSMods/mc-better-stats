package thecsdev.fabric2forge.bss_p758.util;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;

import org.jetbrains.annotations.Nullable;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.stream.JsonReader;

public class JsonHelper
{
	public static final Gson GSON = (new GsonBuilder()).create();
	
	public static JsonObject deserialize(String content, boolean lenient)
	{
	    return deserialize(new StringReader(content), lenient);
	}
	  
	public static JsonObject deserialize(Reader reader, boolean lenient)
	{
		return deserialize(GSON, reader, JsonObject.class, lenient);
	}
	
	@Nullable
	public static <T> T deserialize(Gson gson, Reader reader, Class<T> type, boolean lenient)
	{
		try
		{
			JsonReader jsonReader = new JsonReader(reader);
			jsonReader.setLenient(lenient);
			return (T)gson.getAdapter(type).read(jsonReader);
		}
		catch (IOException iOException)
		{
			throw new JsonParseException(iOException);
		} 
	}
}
