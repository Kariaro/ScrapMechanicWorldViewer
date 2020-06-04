package sm.world;

import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class Block {
	public final UUID uuid = null;
	public final String name = null;
	public final Integer legacyId = 0;
	public final Integer tiling = 0;
	public final String color = null;
	public final String dif = null;
	public final String asg = null;
	public final String nor = null;
	public final Boolean glass = false;
	public final String physicsMaterial = null;
	
	@JsonIgnore
	public final Object ratings = null; // TODO: ????
	
	public final boolean flammable = false;
	public final Double density = 0.0;
	public final Integer qualityLevel = 0;
	
	@Override
	public String toString() {
		return "Block@" + uuid + '@' + name;
	}
}
