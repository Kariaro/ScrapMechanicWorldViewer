package sm.objects;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import sm.sqlite.Sqlite;
import sm.util.Util;

public class BodyList extends SQLiteObject {
	public BodyList(Sqlite sqlite) {
		super(sqlite);
	}
	
	public List<RigidBody> getAllRigidBodies() {
		List<RigidBody> list = new ArrayList<>();
		ResultSet set = sqlite.execute("SELECT * FROM RigidBody");
		try {
			while(set.next()) {
				list.add(new RigidBody(set));
			}
		} catch(Exception e) {
			e.printStackTrace();
		}
		return list;
	}
	
	// TODO: Allow creatin and painting 'shapeType' = '0x1f'
	
	// Multiple per body
	public class ChildShape {
		public int unk_0_1 = 0; // id ???
		/**
		 * <pre>
		 * 0x1f: block
		 * 0x20: part
		 * </pre>
		 */
		public int shapeType_1_1 = 0; // type_id ??? // TODO: Create Enum
		public int unk_2_1 = 0; // id ???
		public int uniqueId_3_4 = 0;
		public int bodyId_7_4 = 0;
		public UUID uuid_11_16 = null;
		public int uniqueId_27_4 = 0;
		
		public int xPos_31_2 = 0; // 31 offset, 2 size
		public int yPos_33_2 = 0; // 33 offset, 2 size
		public int zPos_35_2 = 0; // 35 offset, 2 size
		
		public int color_abgr_37_4 = 0;
		public int rotation_41_1 = 0;
		
		// Only for 'shapeType' == '0x1f'
		public int xs_41_2 = 0; // x size
		public int ys_43_2 = 0; // y size
		public int zs_45_2 = 0; // z size
		
		protected ChildShape(ResultSet childShape) throws SQLException {
			this(childShape.getInt("id"), childShape.getInt("bodyId"), childShape.getBytes("data"));
		}
		
		protected ChildShape(int id, int bodyId, byte[] data) {
			unk_0_1 = data[0];
			shapeType_1_1 = data[1];
			unk_2_1 = data[2];
			uniqueId_3_4 = Util.getInt(data, 3, true);
			bodyId_7_4 = Util.getInt(data, 7, true);
			uuid_11_16 = Util.getUUID(data, 11, false);
			uniqueId_27_4 = Util.getInt(data, 27, true);
			
			xPos_31_2 = Util.getShort(data, 31, true);
			yPos_33_2 = Util.getShort(data, 33, true);
			zPos_35_2 = Util.getShort(data, 35, true);
			color_abgr_37_4 = Util.getInt(data, 37, true);
			rotation_41_1 = Byte.toUnsignedInt(data[41]);
			
			
			if(data.length >= 47) {
				xs_41_2 = Util.getShort(data, 41, true);
				ys_43_2 = Util.getShort(data, 43, true);
				zs_45_2 = Util.getShort(data, 45, true);
			}
			// This data is sometimes absent
			// TODO: Type specific data
			
			/*
			System.out.println("id = " + id);
			System.out.println("bodyId = " + bodyId);
			int a = 1;
			for(byte b : data) {
				System.out.printf("%02x%s", b, ((a++ % 16) == 0) ? "\n":"");
			}
			System.out.println();
			
			
			System.out.println("type = " + shapeType_1_1);
			System.out.println("uniqueId = " + uniqueId_3_4);
			//System.out.println("bodyId = " + bodyId_7_4);
			//System.out.println("BODYID = " + bodyId);
			System.out.println("uuid = " + uuid_11_16);
			System.out.printf("   = x = %d\n", xPos_31_2);
			System.out.printf("   = y = %d\n", yPos_33_2);
			System.out.printf("   = z = %d\n", zPos_35_2);
			System.out.printf("rot = %8s\n", Integer.toBinaryString(rotation_41_1));
			//System.out.println("   = " + Util.getInt(bytes, 39, true));
			*/
			// bot: [z-]
			// top four repeats every four 
			// top is behind bot one
			// when not doing rotation around up axis bot is same
			//  xyz
			// .010 .101
			// .011 .010
			// .110 .011
			// .101 .110
			// .111 .101
			// .111 .010
			// .111 .011
			// .111 .110
			
			// bot: [y-]
			// .101 .001
			// .111 .101
			// .011 .111
			// .001 .011
			// .110 .001
			// .110 .101
			// .110 .111
			// .110 .011
			
			// Displacement rot?
			
			// bit 1 and 5 is the sign of two directions
			//             |Rotation|
			//  A   U   R  | S   S  | z-
			// ..+ +.. .+. | 101 110|  ( 0  0  0)
			// ..+ .-. +.. | 010 101|  ( 1  0  0)
			// ..+ -.. .-. | 011 010|  ( 1 -1  0)
			// ..+ .+. -.. | 110 011|  ( 0 -1  0)
			//             |        | x+
			// .+. -.. ..+ | 011 111|  ( 0  0  1)
			// .+. ..- -.. | 001 011|  ( 0  0  0)
			// .+. +.. ..- | 101 001|  ( 0 -1  0)
			// .+. ..+ +.. | 111 101|  ( 0 -1  1)
			
			
			// all: swaped sign changes bit 1 and 4
			// z-
			// .0 10  .1 01  ( 0  0  0)
			// .0 11  .0 10  ( 1  0  0)
			// .1 10  .0 11  ( 1 -1  0)
			// .1 01  .1 10  ( 0 -1  0)
			
			
			// x+
			// .0 10  .0 01  ( 0  0  1)
			// .1 11  .0 10  ( 0  0  0)
			// .1 10  .1 11  ( 0 -1  0)
			// .0 01  .1 10  ( 0 -1  1)

			// y-
			// .1 11  .1 01  ( 0 -1  0)
			// .0 11  .1 11  ( 1 -1  0)
			// .0 01  .0 11  ( 1 -1  1)
			// .1 01  .0 01  ( 0 -1  1)
			
			// y: swaped sign changes bit 1 and 4
			// y+
			// .0 11  .0 01  ( 0  0  1)
			// .1 11  .0 11  ( 0  0  0)
			// .1 01  .1 11  (-1  0  0)
			// .0 01  .1 01  ( 0 -1  1)
			
			// bot z-:
			//      0: 00100101
			//      1: 00110010
			//      2: 01100011
			//      3: 01010110
			// 
			// bot y+:
			//      0: 00110001
			//      1: 01110011
			//	    2: 01010111
			//	    3: 00010101
			
			// bot x+: 01100001
			
			// bot y-: 01010001
			// bot x-: 00100001
			// bot z+: 00110110
		}
		
		@Override
		public String toString() {
			return uuid_11_16.toString();
		}
	}
	
	// Only one per body
	public class RigidBody {
		private int id;
		private int worldId;
		
		public int unk_0_1 = 0;
		public int isSolid_1_1 = 0;
		public int unk_2_1 = 0;
		public int bodyId_3_4 = 0;
		public int unk_7_3 = 0;
		
		public int unk_10_3 = 0;
		public int unk_11_1 = 0;
		
		public int unk_14_3 = 0;
		public int unk_15_1 = 0;
		
		public int unk_18_3 = 0;
		public int unk_19_1 = 0;
		
		public int unk_22_3 = 0;
		public int unk_23_1 = 0;
		public int z_22_1 = 0;
		
		public RigidBodyBounds bounds;
		public List<ChildShape> shapes;
		
		protected RigidBody(ResultSet rigidBody) throws SQLException {
			shapes = new ArrayList<>();
			id = rigidBody.getInt("id");
			worldId = rigidBody.getInt("worldId");
			byte[] data = rigidBody.getBytes("data");
			
			bounds = new RigidBodyBounds(sqlite.execute("SELECT * FROM RigidBodyBounds WHERE id = " + id));
			//System.out.println(bounds);
			
			ResultSet childSet = sqlite.execute("SELECT * FROM ChildShape WHERE bodyId = " + id);
			while(childSet.next()) {
				shapes.add(new ChildShape(childSet));
			}
			
			unk_0_1 = data[0];
			isSolid_1_1 = data[1];
			bodyId_3_4 = Util.getInt(data, 3, true);
			unk_7_3 = Util.get3Int(data, 7, true);
			
			unk_10_3 = Util.get3Int(data, 10, true);
			unk_11_1 = Byte.toUnsignedInt(data[11]);
			unk_14_3 = Util.get3Int(data, 14, true);
			unk_15_1 = Byte.toUnsignedInt(data[15]);
			unk_18_3 = Util.get3Int(data, 18, true);
			unk_19_1 = Byte.toUnsignedInt(data[19]);
			unk_22_3 = Util.get3Int(data, 22, true); 
			unk_23_1 = Byte.toUnsignedInt(data[23]);
			
			/*
			System.out.println("? " + Integer.toBinaryString(unk_7_3));
			System.out.println("  ? " + unk_10_3 + ", " + Integer.toHexString(unk_10_3));
			System.out.println("  ? " + unk_11_1 + ", " + Integer.toHexString(unk_11_1));
			System.out.println();
			
			System.out.println("  ? " + unk_14_3 + ", " + Integer.toHexString(unk_14_3));
			System.out.println("  ? " + unk_15_1 + ", " + Integer.toHexString(unk_15_1));
			System.out.println();
			
			System.out.println("  ? " + unk_18_3 + ", " + Integer.toHexString(unk_18_3));
			System.out.println("  ? " + unk_19_1 + ", " + Integer.toHexString(unk_19_1));
			System.out.println();
			
			System.out.println("  ? " + unk_22_3 + ", " + Integer.toHexString(unk_22_3));
			System.out.println("  ? " + unk_23_1 + ", " + Integer.toHexString(unk_23_1));
			System.out.println();
			System.out.println();
			*/
		}
	}
	
	// Only one per RigidBody
	public class RigidBodyBounds {
		private int id;
		private double minX;
		private double maxX;
		private double minY;
		private double maxY;
		
		private RigidBodyBounds(ResultSet rigidBodyBounds) throws SQLException {
			id = rigidBodyBounds.getInt("id");
			minX = rigidBodyBounds.getFloat("minX");
			maxX = rigidBodyBounds.getFloat("maxX");
			minY = rigidBodyBounds.getFloat("minY");
			maxY = rigidBodyBounds.getFloat("maxY");
		}
		
		@Override
		public String toString() {
			return new StringBuilder()
				.append("RigidBodyBounds{ ")
				.append(String.format("minX: %8.5f, minY: %8.5f, maxX: %8.5f, maxY: %8.5f", minX, minY, maxX, maxY))
				.append(" }").toString();
		}
	}
}
