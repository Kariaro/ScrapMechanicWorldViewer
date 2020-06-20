package sm.world.tile;

import java.io.File;
import java.util.UUID;

import sm.util.FileUtils;
import sm.util.StringUtils;

final class TileReader {
	public static void main(String[] args) {
		//String path = getTile("TESTING_TILE_FLAT");
		//path = getTile("TESTING_TILE_FLAT_MEDIUM");
		//path = getTile("TESTING_TILE_FLAT_MEDIUM_COLORFLAT_2");
		//path = getTile("TESTING_TILE_FLAT_MEDIUM_COLORFLAT");
		
		//String path = getTile("tile_5");
		String path = getTile("TESTING_TILE_FLAT_MEDIUM_COLORFLAT_2");//tile_9");
		// path = getGameTile("MEADOW128_09");
		// path = "D:\\Steam\\steamapps\\common\\Scrap Mechanic\\Survival\\Terrain\\Tiles\\start_area\\SurvivalStartArea_BigRuin_01.tile";
		// 2134589f
		try {
			loadTile(path);
		} catch(Exception e) {
			e.printStackTrace();
		}
		
		System.out.println("\nCustomMemory:");
		System.out.println(StringUtils.getHexString(customMemory.data(), 1024, 32));
	}
	
	
	private static String getTile(String name) {
		File tile_path = new File("C:/Users/Admin/AppData/Roaming/Axolot Games/Scrap Mechanic/User/User_76561198251506208/Tiles/");
		
		for(File dir_file : tile_path.listFiles()) {
			for(File file : dir_file.listFiles()) {
				if(file.getName().equals(name + ".tile")) return file.getAbsolutePath();
			}
		}
		
		return null;
	}
	
	private static String getGameTile(String name) {
		File tile_path = new File("D:/Steam/steamapps/common/Scrap Mechanic/Data/Terrain/Tiles/");
		
		for(File file : tile_path.listFiles()) {
			if(file.getName().equals(name + ".tile")) return file.getAbsolutePath();
		}
		
		return null;
	}
	
	private static Pointer customMemory;
	
	public static TileHeader loadTile(String path) throws Exception {
		Pointer reader = new Pointer(FileUtils.readFileBytes(path));
		
		String magic = reader.NextString(4, true);
		if(!magic.equals("TILE")) {
			throw new Exception("File magic value was wrong. Should be 'TILE'");
		}
		
		int tileVersion = reader.NextInt(false);
		
		UUID tileUuid;
		long creatorId;
		
		if(tileVersion <= 1000000) {
			tileUuid = reader.NextUuid(true);
			creatorId = reader.NextLong(false);
		} else {
			tileUuid = UUID.randomUUID();
			creatorId = 0;
		}
		
		int tileXSize = reader.NextInt(false);
		int tileYSize = reader.NextInt(false);
		
		int cellHeadersOffset = reader.NextInt(false);
		int cellHeadersSize = reader.NextInt(false);
		int local_c4 = reader.NextInt(false);
		int local_c0 = reader.NextInt(false);
		

		int tileType = 0;
		if(tileVersion <= 1000000) {
			tileType = reader.NextInt(false) >>> 0x18;
		}
		
		System.out.printf("TileFileVersion: %d\n", tileVersion);
		System.out.printf("TileUuid: {%s}\n", tileUuid);
		System.out.printf("CreatorId: %d\n", creatorId);
		System.out.printf("Size: %d, %d\n", tileXSize, tileYSize);
		System.out.printf("Type: %d\n\n", tileType);
		System.out.println("Header info:");
		System.out.printf("CellHeadersOffset: %d\n", cellHeadersOffset);
		System.out.printf("CellHeadersSize: %d\n\n", cellHeadersSize);
		
		TileHeader header = new TileHeader(
			tileVersion,
			creatorId,
			tileUuid,
			tileXSize,
			tileYSize,
			tileType
		);
		
		System.out.println();
		if(reader.index() != cellHeadersOffset) {
			LogError(
				207,
				"Z:\\Jenkins\\workspace\\sm\\TileEditorCommon\\Tile.cpp",
				"pos == header.cellHeadersOffset"
			);
		}
		
		
		if(tileXSize * tileYSize != 0) {
			byte[] headerBytes = new byte[tileXSize * tileYSize * 0x124];
			
			for(int i = 0; i < tileXSize * tileYSize; i++) {
				reader.NextBytes(headerBytes, i * 0x124, cellHeadersSize);
			}
			
			header.fillHeaderBytes(headerBytes);
		}
		
		System.out.println("Headers:");
		
		for(int i = 0; i < tileXSize * tileYSize; i++) {
			int x = i % tileXSize;
			int y = i / tileYSize;
			
			byte[] bytes = header.getHeader(x, y);
			System.out.printf("	BLOB(%d, %d):\n", x, y);
			System.out.printf("		%s\n\n", StringUtils.getHexString(bytes, cellHeadersSize, 32).replace("\n", "\n		"));
		}
		
		System.out.println();
		System.out.println("Reader index = " + reader.index());
		
		customMemory = new Pointer(1000000);
		
		System.out.println("Reading TileHeader:");
		
		// NOTE - Mip - https://en.wikipedia.org/wiki/Mipmap
		if(tileYSize > 0) {
			int iVar12 = tileXSize;
			int iVar13 = tileYSize;
			
			int local_118 = 0;
			int local_114 = 0;
			
			for(int y = 0; y < tileYSize; y++) {
				for(int x = 0; x < tileXSize; x++) {
					Pointer memory = new Pointer(header.getHeader(x, y));
					
					if(tileType == 0) {
						CalculateMip(reader, memory);
						// FUN_00b7f560(*(void **)((int)DAT_0146b85c + 0x9c), _pbVar9, _x, _y, tileVersion);
						
						// CalculateClutter(reader, memory);
						// TerrainGrass(*(void **)((int)DAT_0146b85c + 0xa0), _pbVar9, _x, _y, tileVersion);
					}
					
					//CalculateAssetList(reader, memory);
					//CalculateNode(reader, memory);
					//CalculatePrefab(reader, memory);
					//CalculateBlueprintList(reader, memory);
					//CalculateDecal(reader, memory);
					//CalculateHarvestableList(reader, memory);
					
					System.out.println();
					
					local_114 += 0x40;
				}
				
				local_118 += 0x40;
			}
			
		}
		
		return header;
	}
	
	
	// NOTE - CalculateMip
	private static boolean CalculateMip(Pointer reader, Pointer memory) {
		reader.set(memory.Int());
		
		int mipCompressedSize = memory.Int(0x18);
		int mipSize = memory.Int(0x30);
		byte[] bytes = reader.Bytes(mipCompressedSize);
		
		System.out.printf("  Mip              : %d, %d\n", mipCompressedSize, mipSize);
		System.out.println("  Bytes: " + StringUtils.getHexString(bytes, 96, 32).replace("\n", "\n         "));
		
		int debugSize = CalculateCompressedSize(reader, customMemory, mipSize);
		if(debugSize != mipCompressedSize) {
			System.out.println(debugSize + ", " + mipCompressedSize + ", " + mipSize);
			
			LogError(
				235,
				"Z:\\Jenkins\\workspace\\sm\\TileEditorCommon\\Tile.cpp",
				"debugSize == h.mipCompressedSize[0]"
			);
			
			return false;
		}
		
		return true;
	}
	
	// NOTE - CalculateClutter
	private static boolean CalculateClutter(Pointer reader, Pointer memory) {
		reader.set(memory.Int(0x48));
		
		int clutterCompressedSize = memory.Int(0x4c);
		System.out.printf("  Clutter          : %d\n", clutterCompressedSize);
		
		int debugSize = CalculateCompressedSize(reader, customMemory, memory.Int(0x50));
		if(debugSize != clutterCompressedSize) {
			System.out.println(debugSize + ", " + clutterCompressedSize);
			
			LogError(
				242,
				"Z:\\Jenkins\\workspace\\sm\\TileEditorCommon\\Tile.cpp",
				"debugSize == h.clutterCompressedSize"
			);
			
			return false;
		}
		
		return true;
	}
	
	// NOTE - CalculateAssetList
	private static boolean CalculateAssetList(Pointer reader, Pointer memory) {
		memory.push();
		memory.set(0x74);
		for(int i = 0; i < 4; i++) {
			int assetListCompressedSize = memory.Int();
			int assetListSize = memory.Int(0x10);
			System.out.printf("    Asset[%d]       : %d / %d\n", i, assetListSize, assetListCompressedSize);
			
			int local_aa = memory.Int(-0x20);
			if(local_aa != 0) {
				reader.set(memory.Int(-0x10));
				
				byte[] bytes = reader.Bytes(assetListCompressedSize);
				System.out.println("Length: " + assetListCompressedSize);
				System.out.println("  Bytes: " + StringUtils.getHexString(bytes, 96, 32).replace("\n", "\n         "));
				
				int debugSize = CalculateCompressedSize(reader, customMemory, memory.Int(0x10));
				if(debugSize == assetListCompressedSize) {
					System.out.println(debugSize + ", " + assetListCompressedSize);
					
					LogError(
						254,
						"Z:\\Jenkins\\workspace\\sm\\TileEditorCommon\\Tile.cpp",
						"debugSize == h.assetListCompressedSize[" + i + "]"
					);
					
					//continue LAB_00b83f72
				}
				// FUN_00b8cf70((int *)&local_1a4, (int)_pbVar9, (int *)&local_124, &local_18c, ((uint *)puVar18)[-8], tileVersion);
				
				if(assetListSize != /* local_124 */ 0) {
					LogError(
						256,
						"Z:\\Jenkins\\workspace\\sm\\TileEditorCommon\\Tile.cpp",
						"debugSize == h.assetListSize[" + i + "]"
					);
					
					//continue LAB_00b83f72
				}
			}
			
			memory.move(4);
		}
		System.out.println();
		
		memory.pop();
		return false;
	}
	
	// NOTE - CalculateNode
	private static boolean CalculateNode(Pointer reader, Pointer memory) {
		int bytes_a4 = memory.Int(0xa4);
		int bytes_a8 = memory.Int(0xa8);
		int nodeCompressedSize = memory.Int(0xac);
		int nodeSize = memory.Int(0xb0);
		System.out.printf("  Node             : %d / %d\n", nodeSize, nodeCompressedSize);
		
		if((bytes_a4 == 0) || (bytes_a8 == 0)) return false;
		reader.set(bytes_a8);
		
		byte[] bytes = reader.Bytes(nodeCompressedSize);
		System.out.println("Length: " + nodeCompressedSize);
		System.out.println("  Bytes: " + StringUtils.getHexString(bytes, 96, 32).replace("\n", "\n         "));
		
		int debugSize = CalculateCompressedSize(reader, customMemory, nodeSize);
		if(debugSize != nodeCompressedSize) {
			System.out.println(debugSize + ", " + nodeCompressedSize);
			
			LogError(
				266,
				"Z:\\Jenkins\\workspace\\sm\\TileEditorCommon\\Tile.cpp",
				"debugSize == h.nodeCompressedSize"
			);
			
			//continue LAB_00b83f72
		}
		
		if(debugSize != nodeSize) {
			LogError(
				258,
				"Z:\\Jenkins\\workspace\\sm\\TileEditorCommon\\Tile.cpp",
				"debugSize == h.nodeSize"
			);
			
			//continue LAB_00b83f72
		}
		
		return true;
	}
	
	// NOTE - CalculatePrefab
	private static boolean CalculatePrefab(Pointer reader, Pointer memory) {
		int bytes_c4 = memory.Int(0xc4);
		int bytes_c8 = memory.Int(0xc8);
		int prefabCompressedSize = memory.Int(0xcc);
		int prefabSize = memory.Int(0xd0);
		System.out.printf("  Prefab           : %d / %d\n", prefabSize, prefabCompressedSize);
		
		if((bytes_c4 == 0) || (bytes_c8 == 0)) return false;
		reader.set(bytes_c8);
		
		{
			byte[] bytes = reader.Bytes(prefabCompressedSize);
			System.out.println("Length: " + prefabCompressedSize);
			System.out.println("  Bytes: " + StringUtils.getHexString(bytes, 96, 32).replace("\n", "\n         "));
			
			int debugSize = CalculateCompressedSize(reader, customMemory, prefabSize);
			if(debugSize != prefabCompressedSize) {
				System.out.println(debugSize + ", " + prefabCompressedSize);
				
				LogError(
					277,
					"Z:\\Jenkins\\workspace\\sm\\TileEditorCommon\\Tile.cpp",
					"debugSize == h.prefabCompressedSize"
				);
				
				//continue LAB_00b83f72
			}
			
			// TODO: Something ???? 0xd0
			if(debugSize != prefabSize) {
				LogError(
					279,
					"Z:\\Jenkins\\workspace\\sm\\TileEditorCommon\\Tile.cpp",
					"debugSize == h.prefabSize"
				);
				
				//continue LAB_00b83f72
			}
		}
		
		return true;
	}
	
	// NOTE - CalculateBlueprintList
	private static boolean CalculateBlueprintList(Pointer reader, Pointer memory) {
		int bytes_94 = memory.Int(0x94, false);
		int bytes_98 = memory.Int(0x98, false);
		int blueprintListCompressedSize = memory.Int(0x9c);
		int blueprintListSize = memory.Int(0xa0);
		System.out.printf("  BlueprintList    : %d / %d\n", blueprintListSize, blueprintListCompressedSize);
		
		if((bytes_94 == 0) || (bytes_98 == 0)) return false;
		reader.set(bytes_98);
		
		{
			byte[] bytes = reader.Bytes(blueprintListCompressedSize);
			System.out.println("Length: " + blueprintListCompressedSize);
			System.out.println("  Bytes: " + StringUtils.getHexString(bytes, 96, 32).replace("\n", "\n         "));
			
			int debugSize = 0;
			if(debugSize != blueprintListCompressedSize) {
				System.out.println(debugSize + ", " + blueprintListCompressedSize);
				
				LogError(
					290,
					"Z:\\Jenkins\\workspace\\sm\\TileEditorCommon\\Tile.cpp",
					"debugSize == h.blueprintListCompressedSize"
				);
				
				//continue LAB_00b83f72
			}
			
			if(debugSize != blueprintListSize) {
				LogError(
					292,
					"Z:\\Jenkins\\workspace\\sm\\TileEditorCommon\\Tile.cpp",
					"debugSize == h.blueprintListSize"
				);
				
				//continue LAB_00b83f72
			}
		}
		
		return true;
	}
	
	// NOTE - CalculateDecal
	private static boolean CalculateDecal(Pointer reader, Pointer memory) {
		int bytes_d4 = memory.Int(0xd4);
		int bytes_d8 = memory.Int(0xd8);
		int decalCompressedSize = memory.Int(0xdc);
		int decalSize = memory.Int(0xe0);
		System.out.printf("  Decal            : %d / %d\n", decalSize, decalCompressedSize);
		
		if((bytes_d4 == 0) || (bytes_d8 == 0)) return false;
		reader.set(bytes_d8);
		
		{
			byte[] bytes = reader.Bytes(decalCompressedSize);
			System.out.println("Length: " + decalCompressedSize);
			System.out.println("  Bytes: " + StringUtils.getHexString(bytes, 96, 32).replace("\n", "\n         "));
			
			int debugSize = 0;
			if(debugSize != decalCompressedSize) {
				System.out.println(debugSize + ", " + decalCompressedSize);
				
				LogError(
					301,
					"Z:\\Jenkins\\workspace\\sm\\TileEditorCommon\\Tile.cpp",
					"debugSize == h.decalCompressedSize"
				);
				
				//continue LAB_00b83f72
			}
			
			if(debugSize != decalSize) {
				LogError(
					303,
					"Z:\\Jenkins\\workspace\\sm\\TileEditorCommon\\Tile.cpp",
					"debugSize == h.decalSize"
				);
				
				//continue LAB_00b83f72
			}
		}
		
		return true;
	}
	
	// NOTE - CalculateHarvestableList
	private static boolean CalculateHarvestableList(Pointer reader, Pointer memory) {
		memory.push();
		memory.set(0x104);
		
		for(int i = 0; i < 4; i++) {
			int harvestableListCompressedSize = memory.Int();
			int harvestableListSize = memory.Int(0x10);
			System.out.printf("    Harvestable[%d] : %d / %d\n", i, harvestableListSize, harvestableListCompressedSize);
		
			int value = memory.Int(-0x20);
			if(value != 0) {
				reader.set(memory.Int(-0x10));
				
				{
					byte[] bytes = reader.Bytes(harvestableListCompressedSize);
					System.out.println("Length: " + harvestableListCompressedSize);
					System.out.println("  Bytes: " + StringUtils.getHexString(bytes, 96, 32).replace("\n", "\n		 "));
					
					int debugSize = 0;
					if(debugSize != harvestableListCompressedSize) {
						System.out.println(debugSize + ", " + harvestableListCompressedSize);
						
						LogError(
							314,
							"Z:\\Jenkins\\workspace\\sm\\TileEditorCommon\\Tile.cpp",
							"debugSize == h.harvestableListCompressedSize[" + i + "]"
						);
						
						//continue LAB_00b83f72
					}
					
					if(debugSize != harvestableListSize) {
						LogError(
							316,
							"Z:\\Jenkins\\workspace\\sm\\TileEditorCommon\\Tile.cpp",
							"debugSize == h.harvestableListSize[" + i + "]"
						);
						
						//continue LAB_00b83f72
					}
				}
			}
			
			memory.move(4);
		}
		memory.pop();
		
		return false;
	}
	
	
	
	private static final int[] INT_00e6cab8 = { 0, 1, 2, 1, 0, 4, 4, 4, 0, 0, 0, 0, 0, 1, 1, 2, 0, 3, 1, 3, 1, 4, 2, 7, 0, 2, 3, 6, 1, 5, 3, 5, 1, 3, 4, 4, 2, 5, 6, 7, 7, 0, 1, 2, 3, 3, 4, 6, 2, 6, 5, 5, 3, 4, 5, 6, 7, 1, 2, 4, 6, 4, 4, 5, 7, 2, 6, 5, 7, 6, 7, 7, 0x1000B, 0x10 };
	private static final int[] INT_00e6cbe0 = { 0, 0, 0, -1, -4, 1, 2, 3, 6, 13, 0, 0, 0, 0, 0, 1, 1, 2, 0, 3, 1, 3, 1, 4, 2, 7, 0, 2, 3, 6, 1, 5, 3, 5, 1, 3, 4, 4, 2, 5, 6, 7, 7, 0, 1, 2, 3, 3, 4, 6, 2, 6, 5, 5, 3, 4, 5, 6, 7, 1, 2, 4, 6, 4, 4, 5, 7, 2, 6, 5, 7, 6, 7, 7 };
	
	// The input will always have a size of 1000000
	private static int CalculateCompressedSize(@Type("byte *") Pointer input, @Type("int *") Pointer param_2, @Type("int *") int size) {
		@Type("byte") int local_1;					// byte local_1;
		@Type("uint") int byteLow;					// uint byteLow;
		@Type("int *") int local_5;					// int* local_5;
		@Type("byte *") Pointer bytes;				// byte* bytes;
		@Type("void *") Pointer nextByteAddress;	// void* nextByteAddress;
		@Type("int *") int local_6;					// int* local_6;
		@Type("int *") int local_7;					// int* local_7;
		@Type("int *") int local_8;					// int* local_8;
		@Type("uint") int byteFlagsCopy;			// uint byteFlagsCopy;
		
		
		// This will throw NPE if 'input' is null
		input = new Pointer(1000000, input.data());
		bytes = new Pointer(input);
		nextByteAddress = new Pointer(input);
		
		if(size == 0) {
			return (input.Byte() == 0 ? 1:0) * 2 - 1;
		}
		
		System.out.println();
	// BeginLoop_00703a07:
		do {
			System.out.println("Loop start: " + bytes.index());
			System.out.println(StringUtils.getHexString(param_2.data(), 64 * 10, 64));
			
			@Type("byte")
			int nextByte = bytes.UnsignedByte(); 		// *bytes
			@Type("byte")
			int byteFlags = nextByte >>> 4;
			
			nextByteAddress.set(bytes.index() + 1);	// nextByteAddress = (byte*)bytes + 1;
			byteFlagsCopy = byteFlags;
			
			// if ((int *)((int)local_0 + -0x1a) < param_2) goto LAB_00703a9b;
			boolean skipIF = ((int)size - 0x1a < param_2.index());
			
			if(byteFlags < 9 && !skipIF) {
				byteLow = (int)nextByte & 0xf;
				
				// ((long *)param_2)[0] = *(long *)(nextByteAddress);
				param_2.WriteLong(nextByteAddress.Long());
				
				// local_3 = (int)nextByteAddress + byteFlagsCopy;
				// local_2 = GetShortPointerValue((short *)local_3);
				// byteFlagsCopy = (int)local_2;
				// This could be any type really
				//	 byteFlagsCopy = nextByteAddress.readUnsignedShortOffset(byteFlagsCopy, false);
				byteFlagsCopy = nextByteAddress.UnsignedShort(byteFlagsCopy);
				System.out.println("1 - byteFlagsCopy: " + byteFlagsCopy);
				

				// bytes = (byte *)((short *)local_3 + 1);
				bytes.set(nextByteAddress.index() + byteFlagsCopy + 2);
				
				// local_6 = (int *)((int)param_2 + (int)byteFlagsCopy);
				// local_7 = (int *)((int)local_6 - (int)byteFlagsCopy);
				local_6 = (int)param_2.index() + byteFlagsCopy;
				local_7 = (int)param_2.index();
				
				// if ((byteLow != 0xf) && (7 < local_2)) {
				if((byteLow != 0xf) && (7 < byteFlagsCopy)) {
					// ((18bytes *)local_6)[0] = ((18bytes *)local_7)[0];
					param_2.WriteBytes(
						param_2.Bytes(18),
						18, byteFlagsCopy
					);
					
					// param_2 = (int *)((int)local_6 + (int)byteLow + 4);
					param_2.move(byteFlagsCopy + byteLow +  4);
					
					continue;	// goto BeginLoop_00703a07;
				}
			} else {
				if(byteFlags == 0xf && !skipIF) {
					int index = 0;
					do {
						byteFlags = nextByteAddress.NextUnsignedByte();
						index += byteFlags;
					} while(byteFlags == 0xff);
					
					byteFlagsCopy = index + 0xf;
				}
	// LAB_00703a9b:
				// local_6 = (int *)((int)param_2 + byteFlagsCopy);
				local_6 = param_2.index() + byteFlagsCopy;
				System.out.println("local_6: " + local_6 + "  ,  byteFlagsCopy: " + byteFlagsCopy);
				
				// if((int *)size + -2 < local_6) {
				if(size - 8 < local_6) {
					System.out.println("Return - " + local_6 + "  ,  " + size);
					if(local_6 == size) {
						// memmove(param_2, nextByteAddress, byteFlagsCopy);
						param_2.WriteBytes(nextByteAddress.Bytes(byteFlagsCopy));
						
						int ama = nextByteAddress.index() - bytes.index() + byteFlagsCopy;
						System.out.println("    - " + ama);
						// return (int)(nextByteAddress + byteFlagsCopy - (int)input);
						return ama;
					}
					
					int ama = input.index() - 1 - nextByteAddress.index();
					System.out.println("    - " + ama);
					System.out.println("    - " + input.index() + ", " + nextByteAddress.index());
					// return (int)(input + (-1 - (int)nextByteAddress));
					return ama;
				}
				
				for(int i = 0; i < byteFlagsCopy; i += 8) {
					System.out.println("Write: " + i + "  ,  " + byteFlagsCopy + "  /  " + (nextByteAddress.index() + i) + "  ,  " + nextByteAddress.data().length);
					
					// ((long *)param_2)[0] = *(long *)((byte *)nextByteAddress + index + (int)param_2);
					// param_2 = (int *)((int *)param_2 + 2);
					param_2.NextWriteLong(nextByteAddress.Long(i));
				}
				
				
				// local_3 = (int)nextByteAddress + byteFlagsCopy;
				// local_2 = GetPointerValue_007022b0((short *)local_3);
				
				System.out.println("MAX: " + nextByteAddress.index() + "/" + nextByteAddress.data().length);
				System.out.println("FGS: " + byteFlagsCopy);
				
				// NOTE - local_3 could be any dataType
				//	byteFlagsCopy = nextByteAddress.readShortOffset(byteFlagsCopy, false);
				byteFlagsCopy = nextByteAddress.UnsignedShort(byteFlagsCopy);
				
				// local_7 = (int *)((int)local_6 - byteFlagsCopy);
				local_7 = (int)local_6 - byteFlagsCopy;
			}
			
			byteLow = nextByte & 0xf;
			
			// nextByteAddress = (byte*)local_3 + 2;
			nextByteAddress.move((int)byteFlagsCopy + 2);
			
			if(byteLow == 0xf) {
				int index = 0;
				do {
					nextByte = nextByteAddress.NextUnsignedByte();
					index += nextByte;
				} while(nextByte == 0xff);
				byteLow = index + 0xf;
			}
			
			// NOTE - local_6 = (int *)((int)param_2 + byteFlagsCopy);
			// NOTE - local_7 = param_2
			{
				param_2.push();
				param_2.set(local_6);
				
				if(byteFlagsCopy < 8) {
					// ((int *)local_6)[0] = 0;
					param_2.WriteInt(0);
					
					// ((int *)local_6)[0] = ((int *)local_7)[0];
					param_2.WriteInt(param_2.Int(-byteFlagsCopy));
					
					// local_4 = (&INT_00e6cab8)[byteFlagsCopy];
					int local_4 = INT_00e6cab8[byteFlagsCopy];
					
					// ((int *)local_6)[1] = ((int *)((int)local_7 + local_4))[0];
					param_2.WriteInt(param_2.Int(-byteFlagsCopy + local_4), 4);
					
					// local_8 = (int *)((int)(int *)((int)local_7 + local_4) - (&INT_00e6cbe0)[byteFlagsCopy]);
					local_8 = local_7 + local_4 - INT_00e6cbe0[byteFlagsCopy];
				} else {
					// local_6[0] = local_7[0];
					// local_6[1] = local_7[1];
	
					// ((long *)local_6)[0] = ((long *)local_7)[0];
					param_2.WriteLong(param_2.Long(-byteFlagsCopy));
					
					// local_8 = (int*)local_7 + 2;
					local_8 = local_7 + 8;
				}
				
				param_2.pop();
			}
			
			
			// NOTE - local_6 = (int *)((int)param_2 + byteFlagsCopy);
			local_6 = (int)param_2.index() + byteFlagsCopy;
			
			// local_7 = (int *)local_6 + 2;
			local_7 = local_6 + 8;

			// bytes = (byte *)nextByteAddress;
			bytes.set(nextByteAddress.index());
			
			// param_2 = (int *)((int)local_6 + byteLow + 4);
			param_2.move(byteFlagsCopy + byteLow + 4);
			
			// if ((int *)local_0 - 3 < param_2) {
			if(size - 12 < param_2.index()) {
				// if((int *)((int)local_0 + -5) < param_2) goto LAB_00703c19;
				if((int)size - 5 < param_2.index()) {
					// goto LAB_00703c19;
					return input.index() - 1 - nextByteAddress.index();
				}

				// local_9 = (int *)((int)local_0 + -7);
				// int local_9 = (int)local_0 - 7;
				
				if(local_7 < size - 7) {
					param_2.push();
					
					local_5 = local_7;
					param_2.set(local_7);
					do {
						// local_5[0] = *(int *)((int)((int)(int *)((int)local_8 - (int)local_7) + 0) + (int)local_5);
						// local_5[1] = *(int *)((int)((int)(int *)((int)local_8 - (int)local_7) + 4) + (int)local_5);
						
						// ((int *)local_5)[0] = *(int *)((int)((int)local_8 - (int)local_7) + 0 + (int)local_5);
						// ((int *)local_5)[1] = *(int *)((int)((int)local_8 - (int)local_7) + 4 + (int)local_5);
						
						// ((int *)local_5)[0] = *(int *)((int)local_8 + 0);
						// ((int *)local_5)[1] = *(int *)((int)local_8 + 4);
						param_2.NextWriteLong(param_2.Long(local_8 - local_7 + param_2.index()));
						
						// local_5 = (int *)local_5 + 2;
						local_5 += 8;
					} while(local_5 < size - 7);
					
					param_2.pop();
					
					// local_8 = (int *)((int)local_8 + (int)((int)local_6 - (int)local_7));
					local_8 += size - 7 - local_7;
					local_7 = size - 7;
				}
				
				while(local_7 < param_2.index()) {
					// local_1 = *(byte *)local_8;
					local_1 = param_2.UnsignedByte(-param_2.index() + local_8);
					
					// local_8 = (int *)((int)local_8 + 1);
					local_8 += 1;
					
					// *(byte *)local_7 = local_1;
					param_2.WriteByte(local_1, -param_2.index() + local_7);
					
					// local_7 = (int *)((int)local_7 + 1);
					local_7 += 1;
				}
			} else {
				byte[] local_8_bts = param_2.Bytes(8, -param_2.index() + local_8);
				
				// local_7[0] = local_8[0];
				// local_6[3] = local_8[1];
				// byte[] src, int srcPos, int offset, int length
				param_2.WriteBytes(local_8_bts, 0, 4, -param_2.index() + local_7);
				param_2.WriteBytes(local_8_bts, 4, 4, -param_2.index() + local_6 + 12);
				
				if(0x10 < byteLow + 4) {
					// NOTE - Decompiler says '8' but ((int*) + 4 == 16) ???
					// local_6 = local_6 + 4;
					local_6 += 8;
					
					// local_8 = (int *)((int)local_8 - (int)local_6);
					local_8 -= local_6;
					
					do {
						// local_6[0] = *(int *)((int)((int)local_8 + 0x8) + (int)local_6);
						// local_6[1] = *(int *)((int)((int)local_8 + 0xc) + (int)local_6);
						
						param_2.WriteLong(
							param_2.Long(-param_2.index() + local_8 + local_6 + 8),
							-param_2.index() + local_6
						);
						
						
						// local_6 = local_6 + 2;
						local_6 = local_6 + 8;
					} while(local_6 < param_2.index());
				}
			}
		} while(true);
	}
	
	private static void LogError(int lineIndex, String sourceFile, String message) {
		System.out.println(sourceFile + ":" + lineIndex + " - " + message);
	}
	
	public static @interface Type {
		public String value();
	}
}
