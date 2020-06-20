package sm.world.tile;

import java.util.UUID;

public class TileHeader {
	public int version;
	public long creatorId;
	public UUID uuid;
	public int width;
	public int height;
	public int type;
	
	public byte[][] header;
	public TileHeader(int version, long creatorId, UUID uuid, int width, int height, int type) {
		header = new byte[width * height][0x124];
		this.version = version;
		this.creatorId = creatorId;
		this.uuid = uuid;
		this.width = width;
		this.height = height;
		this.type = type;
	}
	
	public TileHeader fillHeaderBytes(byte[] bytes) {
		for(int y = 0; y < height; y++) {
			for(int x = 0; x < width; x++) {
				int idx = x + y * width;
				System.arraycopy(bytes, idx * 0x124, header[idx], 0, 0x124);
			}
		}
		return this;
	}
	
	public byte[] getHeader(int x, int y) {
		return header[x + y * width];
	}
}
