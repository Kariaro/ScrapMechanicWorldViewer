package sm.objects;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector2f;
import org.joml.Vector3f;

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
		
		try {
			//tryStuffGeneridData();
		} catch(Exception e) {
			e.printStackTrace();
		}
		
		return list;
	}
	
	private static String last = "";
	private void tryStuffGeneridData() throws SQLException {
		ResultSet set = sqlite.execute("SELECT * FROM GenericData WHERE channel = 4");
		
		byte[] data = set.getBytes("data");
		
		int a = 1;
		StringBuilder sb = new StringBuilder();
		for(byte b : data) {
			sb.append(String.format("%02x%s", b, ((a++ % 16) == 0) ? "":""));
		}
		String nows = sb.toString();
		
		int unk_0_4 = Util.getInt(data, 0, true);
		int worldId_4_2 = Short.toUnsignedInt(Util.getShort(data, 4, true));
		int channel_6_1 = Byte.toUnsignedInt(data[6]);
		int flags_7_1 = Byte.toUnsignedInt(data[7]);
		int unk_8_4 = Util.getInt(data, 8, true);
		
		nows = nows.substring(8 * 2);
		
		if(!last.equals(nows)) {
			System.out.println(nows);
			System.out.println("unk_0_4: " + unk_0_4);
			System.out.println("worldId_4_2: " + worldId_4_2);
			System.out.println("channel_6_1: " + channel_6_1);
			System.out.println("flags_7_1: " + flags_7_1);
			System.out.println("unk_8_4: " + unk_8_4);
			System.out.println();
			
			last = nows;
		}
	}
	
	// Multiple per body
	public class ChildShape {
		public int unk_0_1 = 0;
		/**
		 * <pre>
		 * 0x1f: block
		 * 0x20: part
		 * </pre>
		 */
		public int shapeType_1_1 = 0; // type_id ??? // TODO: Create Enum
		public int unk_2_1 = 0;
		
		public int id_3_4 = 0;     // This is just the ChildShape.id
		public int bodyId_7_4 = 0; // This is just the ChildShape.bodyId
		
		/** offset:11 size:16 */ public UUID uuid = null;
		public int uniqueId_27_4 = 0;
		
		/** offset:31 size:2 */ public int xPos = 0;
		/** offset:33 size:2 */ public int yPos = 0;
		/** offset:35 size:2 */ public int zPos = 0;
		
		/** offset:37 size:4 */ public int colorRGBA = 0;
		
		// Only if 'shapeType' == '0x20'
		public int rotation_41_1 = 0;
		
		// Only for 'shapeType' == '0x1f'
		/** offset:41 size:2 */ public int xSize = 0; // x size
		/** offset:43 size:2 */ public int ySize = 0; // y size
		/** offset:45 size:2 */ public int zSize = 0; // z size
		
		
		protected ChildShape(RigidBody body, ResultSet childShape) throws SQLException {
			this(body, childShape.getInt("id"), childShape.getInt("bodyId"), childShape.getBytes("data"));
		}
		
		public int TEST_id;
		public int TEST_bodyId;
		public byte[] TEST_data;
		
		public RigidBody body;
		protected ChildShape(RigidBody body, int id, int bodyId, byte[] data) {
			this.body = body;
			
			TEST_id = id;
			TEST_bodyId = bodyId;
			TEST_data = data;
			
			unk_0_1 = data[0];
			shapeType_1_1 = data[1];
			unk_2_1 = data[2];
			id_3_4 = Util.getInt(data, 3, true);
			bodyId_7_4 = Util.getInt(data, 7, true);
			
			uuid = Util.getUUID(data, 11, false);
			uniqueId_27_4 = Util.getInt(data, 27, true);
			
			xPos = Util.getShort(data, 31, true);
			yPos = Util.getShort(data, 33, true);
			zPos = Util.getShort(data, 35, true);
			
			colorRGBA = Util.getInt(data, 37, false);
			rotation_41_1 = Byte.toUnsignedInt(data[41]);
			
			
			System.out.printf("rot: %8s\n", Integer.toBinaryString(rotation_41_1));
			if(shapeType_1_1 == 0x1f && data.length >= 47) {
				xSize = Util.getShort(data, 41, true);
				ySize = Util.getShort(data, 43, true);
				zSize = Util.getShort(data, 45, true);
			}
			
			// TODO: Type specific data
		}
		
		@Override
		public String toString() {
			return uuid.toString();
		}
	}
	
	// Only one per body
	public class RigidBody {
		public int bodyId;
		public int worldId;
		
		
		// These three could be just one?
		public int isStatic_0_2 = 0; // 1 is static, 2 is dynamic
		public int unk_2_1 = 0;
		
		public int bodyId_3_4 = 0;
		public int unk_7_2 = 0;
		
		// These values are exactly the same as the values inside RigidBodyBounds
		/** offset: 9 size:4 */ public float yMax = 0;
		/** offset:13 size:4 */ public float yMin = 0;
		/** offset:17 size:4 */ public float xMax = 0;
		/** offset:21 size:4 */ public float xMin = 0;
		
		// TODO: Next values
		// Only if 'isStatic' == '2'
		public Quaternionf quat;
		public Matrix4f matrix;
		
		public RigidBodyBounds bounds;
		//public RigidBodyBoundsNode node;
		public List<ChildShape> shapes;

		public byte[] TEST_data;
		
		protected RigidBody(ResultSet rigidBody) throws SQLException {
			shapes = new ArrayList<>();
			bodyId = rigidBody.getInt("id");
			worldId = rigidBody.getInt("worldId");
			byte[] data = rigidBody.getBytes("data");
			TEST_data = data;
			
			bounds = new RigidBodyBounds(sqlite.execute("SELECT * FROM RigidBodyBounds WHERE id = " + bodyId));
			//System.out.println(bounds);
			
			ResultSet childSet = sqlite.execute("SELECT * FROM ChildShape WHERE bodyId = " + bodyId);
			while(childSet.next()) shapes.add(new ChildShape(this, childSet));
			
			//int nodeId = (int)sqlite.executeSingle("SELECT nodeno FROM RigidBodyBounds_rowid WHERE rowid = " + bodyId);
			//node = new RigidBodyBoundsNode(sqlite.execute("SELECT * FROM RigidBodyBounds_node WHERE nodeno = " + nodeId));
			
			
			isStatic_0_2 = Util.getShort(data, 0, true);
			unk_2_1 = data[2];
			bodyId_3_4 = Util.getInt(data, 3, true);
			unk_7_2 = Util.getShort(data, 7, true);
			
			// Bounding box
			xMax = Util.getFloat(data,  9, true);
			xMin = Util.getFloat(data, 13, true);
			yMax = Util.getFloat(data, 17, true);
			yMin = Util.getFloat(data, 21, true);
			
			// 2 bytes space
			if(isStatic_0_2 == 2) {
				float aa = Util.getFloat(data, 27, true);
				float bb = Util.getFloat(data, 31, true);
				float cc = Util.getFloat(data, 35, true);
				float dd = Util.getFloat(data, 39, true);
				
				float ee = Util.getFloat(data, 43, true);
				float ff = Util.getFloat(data, 47, true);
				float gg = Util.getFloat(data, 51, true);
				
				float hh = Util.getFloat(data, 55, true);
				float ii = Util.getFloat(data, 59, true);
				float jj = Util.getFloat(data, 63, true);
				
				float kk = Util.getFloat(data, 67, true);
				float ll = Util.getFloat(data, 71, true);
				float mm = Util.getFloat(data, 75, true);
				// 40609a36
				// 3fc08ff0
				// bf7e4257
				
				// bcd74b74
				// 3c9bdcfb
				// bd1017b8
				
				// bdbf2b3b
				// bd285d82
				// b8e3d010
				//00
				//System.out.println("AB: " + aa + ", " + bb);
				//System.out.println("CD: " + cc + ", " + dd);
				System.out.printf("EFG: %8.5f, %8.5f, %8.5f\n", ee, ff, gg);
				System.out.printf("HIJ: %8.5f, %8.5f, %8.5f\n", hh, ii, jj);
				System.out.printf("KLM: %8.5f, %8.5f, %8.5f\n", kk, ll, mm);
				
				quat = new Quaternionf(aa, bb, cc, dd);
				matrix = quat.get(new Matrix4f());
				//System.out.println("Quaternion: " + quat);
				System.out.println("Matrix3x3: \n" + matrix.toString(NumberFormat.getNumberInstance()));
				
				int a = 1;
				StringBuilder sb = new StringBuilder();
				for(int i = 43; i < data.length; i++) {
					byte b = data[i];
					sb.append(String.format("%02x%s", b, ((a++ % 16) == 0) ? "":""));
				}
				String nows = sb.toString();
				
				if(!nows.equals(last)) {
					
					System.out.println(nows);
					last = nows;
				}
			}
			
			// System.out.println("XX: " + xmm_9_4 + ",  " + xmx_13_4);
			// System.out.println("ZZ: " + zmm_17_4 + ", " + zmx_21_4);
			// System.out.println();
		}
		
		public Vector3f getMiddleLocal() {
			Vector2f xSize = new Vector2f(Float.MAX_VALUE, -Float.MAX_VALUE);
			Vector2f ySize = new Vector2f(Float.MAX_VALUE, -Float.MAX_VALUE);
			Vector2f zSize = new Vector2f(Float.MAX_VALUE, -Float.MAX_VALUE);
			
			for(ChildShape shape : shapes) {
				if(shape.shapeType_1_1 != 0x1f) continue;
				
				float xs = shape.xPos;
				float xe = xs + shape.xSize;
				float ys = shape.yPos;
				float ye = ys + shape.ySize;
				float zs = shape.zPos;
				float ze = zs + shape.zSize;
				
				if(xs < xSize.x) xSize.x = xs;
				if(ys < ySize.x) ySize.x = ys;
				if(zs < zSize.x) zSize.x = zs;
				
				if(xe > xSize.y) xSize.y = xe;
				if(ye > ySize.y) ySize.y = ye;
				if(ze > zSize.y) zSize.y = ze;
			}
			
			return new Vector3f(
				(xSize.x + xSize.y) / 2.0f,
				(ySize.x + ySize.y) / 2.0f,
				(zSize.x + zSize.y) / 2.0f
			);
		}
		
		public Vector3f getMiddle() {
			float x = 0;
			float y = 0;
			float z = 0;
			
			
			for(ChildShape shape : shapes) {
				if(shape.shapeType_1_1 != 0x1f) continue;
				
				// TODO: What about parts????
				int sx = shape.yPos;
				int sy = shape.zPos;
				int sz = shape.xPos;
				
				int ssz = shape.ySize;
				int ssy = shape.zSize;
				int ssx = shape.xSize;
				
				// Correct
				float smx = sx + (ssx) / 2.0f;
				float smy = sy + (ssy) / 2.0f;
				float smz = sz + (ssz) / 2.0f;
				
				x += smx;
				y += smy;
				z += smz;
			}
			
			if(shapes.size() > 0) {
				x /= (float)shapes.size();
				y /= (float)shapes.size();
				z /= (float)shapes.size();
			}
			
			return new Vector3f(x, y, z);
		}
	}
	
	// Only one per RigidBody
	public class RigidBodyBounds {
		private final int id;
		public final float yMin;
		public final float yMax;
		public final float xMin;
		public final float xMax;
		
		private RigidBodyBounds(ResultSet rigidBodyBounds) throws SQLException {
			id = rigidBodyBounds.getInt("id");
			xMin = rigidBodyBounds.getFloat("minX");
			xMax = rigidBodyBounds.getFloat("maxX");
			yMin = rigidBodyBounds.getFloat("minY");
			yMax = rigidBodyBounds.getFloat("maxY");
		}
		
		public int getId() {
			return id;
		}
		
		@Override
		public String toString() {
			return new StringBuilder()
				.append("RigidBodyBounds{ ")
				.append(String.format("minX: %8.5f, minY: %8.5f, maxX: %8.5f, maxY: %8.5f", yMin, xMin, yMax, xMax))
				.append(" }").toString();
		}
	}
	
	public class RigidBodyBoundsNode {
		private int nodeno;
		public int size_0_4;
		
		// Arrays
		
		
		public byte[] TEST_data;
		private RigidBodyBoundsNode(ResultSet node) throws SQLException {
			nodeno = node.getInt("nodeno");
			byte[] data = node.getBytes("data");
			TEST_data = data;
			
			size_0_4 = Util.getInt(data, 0, true);
			
			for(int i = 0; i < size_0_4; i++) {
				int offset = 8 + i * 24;
				// 24 bytes between each entry
				// 16 bytes entry
				int bodyId_0_4 = Util.get3Int(data, offset, true);
				float xmin_4_4 = Util.getFloat(data, offset + 4, true);
				float xmax_8_4 = Util.getFloat(data, offset + 8, true);
				float zmin_12_4 = Util.getFloat(data, offset + 12, true);
				float zmax_16_4 = Util.getFloat(data, offset + 16, true);
			}
		}
	}
}
