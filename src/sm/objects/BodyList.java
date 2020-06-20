package sm.objects;

import java.sql.Blob;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.sqlite.jdbc4.JDBC4Connection;

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
	
	// Only one per body
	public class RigidBody {
		public int bodyId;
		public int worldId;
		
		
		// These three could be just one?
		public int isStatic_0_2 = 0; // 1 is static, 2 is dynamic
		public int unk_2_1 = 0;
		
		/** offset:3 size:4 */ public int bodyId_3_4 = 0;
		public int unk_7_2 = 0;

		
		// These values only shows up when 'isStatic' == '1'
		// 19 == onLift???
		// -1 == none???
		/** offset:56 size:4 */ public int staticFlags = 0;
		
		// These values are exactly the same as the values inside RigidBodyBounds
		/** offset: 9 size:4 */ public float yMax = 0;
		/** offset:13 size:4 */ public float yMin = 0;
		/** offset:17 size:4 */ public float xMax = 0;
		/** offset:21 size:4 */ public float xMin = 0;
		
		// These values only shows up when 'isStatic' == '2'
		/** offset:27 size:16 */public Quaternionf quat;
		
		/** offset:43 size:4 */ public float xWorld = 0;
		/** offset:47 size:4 */ public float yWorld = 0;
		/** offset:51 size:4 */ public float zWorld = 0;
		/** offset:55 size:4 */ public float xVelocity = 0;
		/** offset:59 size:4 */ public float yVelocity = 0;
		/** offset:63 size:4 */ public float zVelocity = 0;
		/** offset:67 size:4 */ public float xAngularVelocity = 0;
		/** offset:71 size:4 */ public float yAngularVelocity = 0;
		/** offset:75 size:4 */ public float zAngularVelocity = 0;
		
		// 1	erasable
		// 2	buildable
		// 4	paintable
		// 8	connectable
		// 16	liftable
		// 32	usable
		// 64	destructable
		// 128	convertibleToDynamic
		/** offset:79 size:1 */ public int flags = 0;
		
		// TODO: Next values
		// Only if 'isStatic' == '2'
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
			
			float aa = Util.getFloat(data, 27, true);
			float bb = Util.getFloat(data, 31, true);
			float cc = Util.getFloat(data, 35, true);
			float dd = Util.getFloat(data, 39, true);
			quat = new Quaternionf(aa, bb, cc, dd);
			matrix = quat.get(new Matrix4f());
			
			xWorld = Util.getFloat(data, 43, true);
			yWorld = Util.getFloat(data, 47, true);
			zWorld = Util.getFloat(data, 51, true);
			
			if(isStatic_0_2 == 1) {
				// This value has with how the object is stuck....
				staticFlags = Util.getInt(data, 55, true);
				//System.out.println("55: " + staticFlags);
				
				staticFlags = data[59];
			}
			
			//System.out.println(isStatic_0_2 + ", " + data.length + ", " + staticFlags);
			// 2 bytes space
			
			/*try {
				JDBC4Connection conn = sqlite.getConnection();
				if(isStatic_0_2 == 2) data[79] = 0;
				data[59] = 0;
				
				data[55] = 1;
				data[56] = 0;
				data[57] = 0;
				data[58] = 0;
				
				PreparedStatement statement = conn.prepareStatement("UPDATE RigidBody SET data = ? WHERE id = " + bodyId);
				statement.setBytes(1, data);
				statement.execute();
			} catch(Exception e) {
				e.printStackTrace();
			}*/
			
			if(isStatic_0_2 == 2) {
				xVelocity = Util.getFloat(data, 55, true);
				yVelocity = Util.getFloat(data, 59, true);
				zVelocity = Util.getFloat(data, 63, true);
				
				xAngularVelocity = Util.getFloat(data, 67, true);
				yAngularVelocity = Util.getFloat(data, 71, true);
				zAngularVelocity = Util.getFloat(data, 75, true);
				
				flags = Byte.toUnsignedInt(data[79]);
				
				if(bodyId == 1566) { // 1531
					//System.out.println();
					//System.out.printf("Quaternion:      %8.5f, %8.5f, %8.5f, %8.5f\n", aa, bb, cc, dd);
					//System.out.printf("WorldPosition:   %8.5f, %8.5f, %8.5f\n", xWorld, yWorld, zWorld);
					//System.out.printf("Velocity:        %8.5f, %8.5f, %8.5f\n", xVelocity, yVelocity, zVelocity);
					//System.out.printf("AngularVelocity: %8.5f, %8.5f, %8.5f\n", xAngularVelocity, yAngularVelocity, zAngularVelocity);
					//System.out.printf("WorldB: %8.5f, %8.5f, %8.5f, %8.5f\n", xMin, xMax, yMin, yMax);
					//System.out.printf("Bounds: %s\n", ShapeUtils.getBoundingBox(this));
					
					//System.out.println(flags);
					
					int a = 1;
					StringBuilder sb = new StringBuilder();
					for(int i = 0; i < data.length; i++) {
						byte b = data[i];
						sb.append(String.format("%02x%s", b, ((a++ % 16) == 0) ? "":""));
					}
					String nows = sb.toString();
					
					if(!nows.equals(last)) {
						
						//System.out.println(nows);
						//last = nows;
					} else {

						//System.out.println(nows);
					}
				}
			}
		}
		
		public boolean isErasable() { return (flags & 1) > 0; }
		public boolean isBuildable() { return (flags & 2) > 0; }
		public boolean isPaintable() { return (flags & 4) > 0; }
		public boolean isConnectable() { return (flags & 8) > 0; }
		public boolean isLiftable() { return (flags & 16) > 0; }
		public boolean isUsable() { return (flags & 32) > 0; }
		public boolean isDestructable() { return (flags & 64) > 0; }
		public boolean isConvertibleToDynamic() { return (flags & 128) > 0; }
	}
	

	// Multiple per body
	public class ChildShape {
		//public int unk_0_1 = 0;
		/**
		 * <pre>
		 * 0x1f: block
		 * 0x20: part
		 * </pre>
		 */
		/** offset:1 size:1 */ public int shapeType = 0; // type_id ??? // TODO: Create Enum
		//public int unk_2_1 = 0;
		
		/** offset:3 size:4 */ public int shapeId = 0;
		/** offset:7 size:4 */ public int bodyId = 0;
		
		/** offset:11 size:16 */ public UUID uuid = null;
		///** offset:27 size:4 */ public int uniqueId_27_4 = 0;
		
		/** offset:31 size:2 */ public int xPos = 0;
		/** offset:33 size:2 */ public int yPos = 0;
		/** offset:35 size:2 */ public int zPos = 0;
		
		/** offset:37 size:4 */ public int colorRGBA = 0;
		
		// Only if 'shapeType' == '0x20'
		/** offset:41 size:1 */ public int partRotation = 0;
		
		// Only if 'shapeType' == '0x1f'
		/** offset:41 size:2 */ public int xSize = 0;
		/** offset:43 size:2 */ public int ySize = 0;
		/** offset:45 size:2 */ public int zSize = 0;
		
		
		protected ChildShape(RigidBody body, ResultSet childShape) throws SQLException {
			this(body, childShape.getInt("id"), childShape.getInt("bodyId"), childShape.getBytes("data"));
		}
		
		public int TEST_id;
		public int TEST_bodyId;
		public byte[] TEST_data;
		
		public RigidBody body;
		protected ChildShape(RigidBody body, int shapeId, int bodyId, byte[] data) {
			this.body = body;
			
			TEST_id = shapeId;
			TEST_bodyId = bodyId;
			TEST_data = data;
			
			//unk_0_1 = data[0];
			shapeType = Byte.toUnsignedInt(data[1]);
			//unk_2_1 = data[2];
			
			this.shapeId = Util.getInt(data, 3, true);
			this.bodyId = Util.getInt(data, 7, true);
			
			uuid = Util.getUUID(data, 11, false);
			// BodyId uniqueId_27_4 = Util.getInt(data, 27, true);
			
			xPos = Util.getShort(data, 31, true);
			yPos = Util.getShort(data, 33, true);
			zPos = Util.getShort(data, 35, true);
			
			colorRGBA = Util.getInt(data, 37, false);
			
			if(shapeType == 0x1f) {
				xSize = Util.getShort(data, 41, true);
				ySize = Util.getShort(data, 43, true);
				zSize = Util.getShort(data, 45, true);
			} else {
				partRotation = Byte.toUnsignedInt(data[41]);
			}
		}
		
		@Override
		public String toString() {
			return uuid.toString();
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
