package sm.lwjgl.mesh;

import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.lwjgl.opengl.GL20;

import sm.asset.ScrapMechanicAssets;
import sm.lwjgl.shader.BlockShader;
import sm.objects.BodyList.ChildShape;
import sm.objects.BodyList.RigidBody;
import sm.world.types.Block;

public class WorldBlockRender {
	private static BlockMesh mesh;
	private final Block block;
	private final Texture dif;
	private final Texture asg;
	private final Texture nor;
	private final BlockShader shader;
	
	// TODO: Unloading!
	public WorldBlockRender(Block block, BlockShader shader) {
		this.shader = shader;
		this.block = block;
		
		if(mesh == null) {
			mesh = new BlockMesh();
		}
		
		Texture block_dif = Texture.NONE,
				block_asg = Texture.NONE,
				block_nor = Texture.NONE;
		
		try {
			block_dif = Texture.loadTexture(ScrapMechanicAssets.resolvePath(block.dif), 0, GL20.GL_LINEAR);
			block_asg = Texture.loadTexture(ScrapMechanicAssets.resolvePath(block.asg), 1, GL20.GL_LINEAR);
			block_nor = Texture.loadTexture(ScrapMechanicAssets.resolvePath(block.nor), 2, GL20.GL_LINEAR);
		} catch(Exception e) {
			e.printStackTrace();
		}
		
		this.dif = block_dif;
		this.asg = block_asg;
		this.nor = block_nor;
	}
	
	public static StringBuilder last;
	public static String last_str;
	public static byte[] dif_bytes;
	
	public void render(ChildShape shape) {
		float x = shape.xPos;
		float y = shape.yPos;
		float z = shape.zPos;
		
		//System.out.println(Integer.toHexString(shape.TEST_bodyId));
		RigidBody body = shape.body;
		if(body.isStatic_0_2 == 2) {
			/*
			//System.out.println(x + ", " + y + ", " + z);
			//System.out.println(Util.getShort(shape.TEST_data, 33, true));
			//System.out.println(Integer.toHexString(shape.TEST_id));
			//RigidBodyBoundsNode node = body.node;
			
			// System.out.println(body.xmm_9_4 + ", " + body.xmx_13_4);
			
			byte[] data = shape.TEST_data;
			int a = 1;
			StringBuilder sb = new StringBuilder();
			for(byte b : data) {
				sb.append(String.format("%02x%s", b, ((a++ % 16) == 0) ? "":""));
			}
			String nows = sb.toString();
			
			if(last == null) {
				last = new StringBuilder(nows);
				dif_bytes = new byte[nows.length()];
				for(int i = 0; i < dif_bytes.length; i++) {
					dif_bytes[i] = '.';
				}
			} else {
				int size = Math.min(last.length(), nows.length());
				if(dif_bytes.length < size) {
					dif_bytes = new byte[size];
				}
				
				for(int i = 0; i < size; i++) {
					char aa = last.charAt(i);
					char bb = nows.charAt(i);
					
					if(aa != bb) {
						dif_bytes[i] = '#';
					} else {
						dif_bytes[i] = '.';
					}
				}
				
				
				if(!nows.equals(last_str)) {
					//System.out.println(nows);
					//System.out.println(new String(dif_bytes));
					
					last_str = nows;
					last.delete(0, last.length());
					last.append(nows);
				}
				
				//System.out.println();
			}*/
		}
		
		if(shape.body.isStatic_0_2 == 2) {
			Vector4f right = shape.body.matrix.getColumn(1, new Vector4f());
			Vector4f up = shape.body.matrix.getColumn(2, new Vector4f());
			Vector4f at = shape.body.matrix.getColumn(0, new Vector4f());
			
			
			float pi = (float)Math.PI;
			Matrix4f matrix = new Matrix4f();
			//matrix.rotate(-pi / 2.0f, new Vector3f(-1, 0, 0));
			//matrix.rotate(shape.body.quat);
			matrix.translate(x, y, z);
			shader.setUniform("transformationMatrix", matrix);
		} else {
			shader.setUniform("transformationMatrix", new Matrix4f().translate(x, y, z));
		}
		
		//System.out.println(shape.uuid);
		if(body.isStatic_0_2 == 1) {
			byte[] data = shape.TEST_data;
			int a = 1;
			StringBuilder sb = new StringBuilder();
			for(byte b : data) {
				sb.append(String.format("%02x%s", b, ((a++ % 16) == 0) ? "":""));
			}
			String nows = sb.toString();
			
			float xx = x;
			float yy = y;
			float zz = z;
			
			//System.out.println(body.zMin);
			
			Vector3f local = body.getMiddleLocal();
			float bxm = (body.xMax + body.xMin) * 2;
			float bzm = (body.zMax + body.zMin) * 2;
			
			Matrix4f matrix = new Matrix4f();
			matrix.translate(
				xx,// - local.x + bxm,
				yy,// - local.y,
				zz// - local.z + bzm
			);
			shader.setUniform("transformationMatrix", matrix);
		}
		
		shader.setUniform("localTransform", x, y, z);
		shader.setUniform("tiling", block.tiling);
		shader.setUniform("scale",
			shape.xSize,
			shape.ySize,
			shape.zSize
		);
		
		{
			int rgba = shape.colorRGBA;
			float r, g, b, a;
			{
				r = ((rgba >> 24) & 0xff) / 255.0f;
				g = ((rgba >> 16) & 0xff) / 255.0f;
				b = ((rgba >>  8) & 0xff) / 255.0f;
				a = ((rgba      ) & 0xff) / 255.0f;
			}
			//r = (x % 4) / 4.0f;
			//g = (y % 4) / 4.0f;
			//b = (z % 4) / 4.0f;
			shader.setUniform("color", r, g, b, a);
		}
		
		dif.bind();
		asg.bind();
		nor.bind();
		//GL11.glEnable(GL11.GL_BLEND);
		//GL11.glEnable(GL11.GL_ALPHA_TEST);
		//GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
		
		mesh.render();
		
		//GL11.glDisable(GL11.GL_ALPHA_TEST);
		//GL11.glDisable(GL11.GL_BLEND);
		
		dif.unbind();
		asg.unbind();
		nor.unbind();
	}
}
