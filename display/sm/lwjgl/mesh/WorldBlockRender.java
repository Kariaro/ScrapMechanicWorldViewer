package sm.lwjgl.mesh;

import org.joml.Matrix4f;
import org.lwjgl.opengl.GL20;

import sm.lwjgl.shader.BlockShader;
import sm.objects.BodyList.ChildShape;
import sm.world.Block;
import sm.world.World;

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
			block_dif = Texture.loadTexture(World.getPath(block.dif), 0, GL20.GL_LINEAR);
			block_asg = Texture.loadTexture(World.getPath(block.asg), 1, GL20.GL_LINEAR);
			block_nor = Texture.loadTexture(World.getPath(block.nor), 2, GL20.GL_LINEAR);
		} catch(Exception e) {
			e.printStackTrace();
		}
		
		this.dif = block_dif;
		this.asg = block_asg;
		this.nor = block_nor;
	}
	
	public void render(ChildShape shape) {
		float x = shape.yPos_33_2;
		float y = shape.zPos_35_2;
		float z = shape.xPos_31_2;
		
		shader.setUniform("transformationMatrix", new Matrix4f().translate(x, y, z));
		shader.setUniform("tiling", block.tiling);
		shader.setUniform("scale",
			shape.ys_43_2,
			shape.zs_45_2,
			shape.xs_41_2
		);
		
		{
			int rgba = shape.color_abgr_37_4;
			float r, g, b, a;
			{
				a = ((rgba >> 24) & 0xff) / 255.0f;
				r = ((rgba      ) & 0xff) / 255.0f;
				g = ((rgba >>  8) & 0xff) / 255.0f;
				b = ((rgba >> 16) & 0xff) / 255.0f;
			}
			// r = ((x + y + z) * 1000 % 255) / 255.0f;
			shader.setUniform("color", r, g, b, a);
		}
		
		dif.bind();
		asg.bind();
		nor.bind();
		
		mesh.render();
		
		dif.unbind();
		asg.unbind();
		nor.unbind();
	}
}
