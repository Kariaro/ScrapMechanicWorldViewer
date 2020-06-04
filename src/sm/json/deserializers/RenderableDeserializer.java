package sm.json.deserializers;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.node.ObjectNode;

import sm.util.FileUtils;
import sm.world.Renderable;
import sm.world.World;

@Deprecated
public class RenderableDeserializer extends JsonDeserializer<Renderable> {
	public Renderable deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JsonProcessingException {
		JsonParser json;
		if(p.hasTextCharacters()) {
			JsonFactory factory = new JsonFactory();
			json = factory.createParser(FileUtils.readFile(World.getPath(p.getText())));
			json.nextValue();
		} else {
			json = p;
		}
		
		System.out.println("---------------------------------");
		int max = 0;
		while(true) {
			String name = json.nextFieldName();
			System.out.println(" nm:" + name);
			if(max ++ > 100) break;
		}
		
		//json.nextValue();
		//json.nextValue();
		
		JsonToken token = json.nextValue();
		System.out.println("    " + token);
		System.out.println("    " + json.nextFieldName());
		
		return null;
	}
}
