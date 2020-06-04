package sm.world;

import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

public class Part {
	public final UUID uuid = null;
	public final String name = null;
	public final int legacyId = 0;
	@JsonDeserialize()
	public final Renderable renderable = null;
	public final String color = null;
	@JsonIgnore
	public final Object cylinder = null; // TODO: ???
	
	@Override
	public String toString() {
		return "Part@" + uuid + '@' + name;
	}
}
