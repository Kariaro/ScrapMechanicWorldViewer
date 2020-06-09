package sm.world.tile;

import java.util.UUID;

import sm.util.FileUtils;
import sm.util.Util;

class TileReader {
	public static final void main(String[] args) {
		String path = getPath("7b6c4786-c915-4d50-92cb-27846aeb37e8/TESTING_TILE_FLAT.tile");
		//path = getPath("cae99b97-ed1e-4f37-814d-c979654db218/TESTING_TILE_FLAT_MEDIUM.tile");
		path = getPath("660f750f-ee72-4a59-becb-ed6ca4dc9502/TESTING_TILE_FLAT_MEDIUM_COLORFLAT_2.tile");
		//path = getPath("acae4b1c-1ecc-4b2b-b469-7da8178579af/TESTING_TILE_FLAT_MEDIUM_COLORFLAT.tile");
		
		
		try {
			loadTile(path);
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	private static final String getPath(String name) {
		return "C:/Users/Admin/AppData/Roaming/Axolot Games/Scrap Mechanic/User/User_76561198251506208/Tiles/" + name;
	}
	
	public static TileReader loadTile(String path) throws Exception {
		byte[] data = FileUtils.readFileBytes(path);
		String magic = Util.getString(data, 0, 4, true);
		if(!magic.equals("TILE")) {
			throw new Exception("File magic value was wrong. Should be 'TILE'");
		}
		int tileFileVersion = Util.getInt(data, 4, false);
		System.out.println("TileFileVersion: " + tileFileVersion);
		
		UUID tileUuid = Util.getUUID(data, 8, true);
		System.out.println("TileUuid: {" + tileUuid + "}");
		
		long creatorId = Util.getLong(data, 24, false);
		System.out.println("CreatorId: " + creatorId);
		
		int xSize = Util.getInt(data, 32, false);
		int ySize = Util.getInt(data, 36, false);
		System.out.println("Size: " + xSize + "x" + ySize);
		
		for(int i = 0; i < 100; i++) {
			int test = Util.getInt(data, 40 + i * 4, false);
			System.out.printf("test[%2d]: %8s, %d\n", i, Integer.toHexString(test), test);
		}
		
		int a = 1;
		for(int i = 40; i < data.length; i++) {
			System.out.printf("%02x", data[i]);
			if((a ++) % 64 == 0) System.out.println();
		}
		
		return null;
	}
}
