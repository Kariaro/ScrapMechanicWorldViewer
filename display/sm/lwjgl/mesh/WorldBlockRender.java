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
import sm.world.types.ShapeUtils.Bounds3D;

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
	
	public void render(ChildShape shape, Bounds3D bounds) {
		float x = shape.xPos;
		float y = shape.yPos;
		float z = shape.zPos;
		
		RigidBody body = shape.body;
		
		//System.out.println(shape.bodyId);
		if(shape.body.isStatic_0_2 == 2) {
			float xm = (bounds.xMin + bounds.xMax) / 2.0f;
			float ym = (bounds.yMin + bounds.yMax) / 2.0f;
			float zm = (bounds.zMin + bounds.zMax) / 2.0f;
			
			//float axm = (body.xMin + body.xMax) / 2.0f;
			//float aym = (body.yMin + body.yMax) / 2.0f;
			Matrix4f matrix = new Matrix4f();
			/*matrix.translate(-xm, -ym, -zm);
			
			matrix.translate(x, y, z);
			matrix.rotateLocal(body.quat);
			matrix.translateLocal(
				(body.yMin + body.yMax) * 2,
				(body.xMin + body.xMax) * 2,
				0
			);*/
			
			//matrix.translate(-xm, -ym, -zm);
			matrix.translateLocal(x, y, z);
			//matrix.translateLocal(-xm, -ym, -zm);
			//matrix.rotateAroundLocal(body.quat, xm, ym, zm);
			/*matrix.translate(
				body.yPos,
				body.xPos,
				body.zPos
			);*/
			/*System.out.printf("%6.3f, %6.3f, %6.3f, %6.3f\n",
				body.xMin, body.xMax,
				body.yMin, body.yMax
			);*/
			//matrix.rotateLocal(body.quat);
			//matrix.rotateAround(body.quat, (body.yMin + body.yMax) * 2, (body.xMin + body.xMax) * 2, 0);
			
			/*{
				byte[] data = body.TEST_data;
				int a = 1;
				StringBuilder sb = new StringBuilder();
				for(int i = 0; i < data.length; i++) {
					byte b = data[i];
					sb.append(String.format("%02x%s", b, ((a++ % 16) == 0) ? "":""));
				}
				String nows = sb.toString();
				
				//System.out.println(nows);
			}*/
			
			//matrix.rotate(body.quat);
			shader.setUniform("transformationMatrix", matrix);
		} else {
			shader.setUniform("transformationMatrix", new Matrix4f().translate(x, y, z));
		}
		
		/* else {
			float xm = (bounds.xMin + bounds.xMax) / 2.0f;
			float ym = (bounds.yMin + bounds.yMax) / 2.0f;
			float zm = 0;//(bounds.zMin + bounds.zMax) / 2.0f;
			
			Matrix4f matrix = new Matrix4f().translate(x, y, z);
			matrix.translate(-xm, -ym, -zm);
			matrix.translate(
				(body.yMin + body.yMax) * 2,
				(body.xMin + body.xMax) * 2,
				0
			);
			shader.setUniform("transformationMatrix", matrix);
		}*/
		
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
		
		// TODO: Transparency
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
