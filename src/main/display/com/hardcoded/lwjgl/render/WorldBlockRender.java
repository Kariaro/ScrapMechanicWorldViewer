package com.hardcoded.lwjgl.render;

import org.joml.Matrix4f;
import org.lwjgl.opengl.GL20;

import com.hardcoded.asset.ScrapMechanicAssetHandler;
import com.hardcoded.lwjgl.mesh.BlockMesh;
import com.hardcoded.lwjgl.mesh.Texture;
import com.hardcoded.lwjgl.shader.BlockShader;
import com.hardcoded.sm.objects.BodyList.ChildShape;
import com.hardcoded.sm.objects.BodyList.RigidBody;
import com.hardcoded.world.types.SMBlock;
import com.hardcoded.world.types.ShapeUtils.Bounds3D;

/**
 * A block renderer.
 * 
 * @author HardCoded
 * @since v0.1
 * 
 * TODO: Transparency
 */
public class WorldBlockRender {
	private static BlockMesh mesh;
	private final SMBlock block;
	private final Texture dif;
	private final Texture asg;
	private final Texture nor;
	private final BlockShader shader;
	
	public WorldBlockRender(SMBlock block, BlockShader shader) {
		this.shader = shader;
		this.block = block;
		
		if(mesh == null) {
			mesh = new BlockMesh();
		}
		
		Texture block_dif = Texture.NONE,
				block_asg = Texture.NONE,
				block_nor = Texture.NONE;
		
		try {
			block_dif = Texture.loadTexture(ScrapMechanicAssetHandler.resolvePath(block.dif), 0, GL20.GL_LINEAR);
			block_asg = Texture.loadTexture(ScrapMechanicAssetHandler.resolvePath(block.asg), 1, GL20.GL_LINEAR);
			block_nor = Texture.loadTexture(ScrapMechanicAssetHandler.resolvePath(block.nor), 2, GL20.GL_LINEAR);
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
		
		if(shape.body.isStatic_0_2 == 2) {
			Matrix4f matrix = new Matrix4f();
			matrix.translateLocal(
				x + body.xWorld * 4,
				y + body.yWorld * 4,
				z + body.zWorld * 4
			);
			matrix.rotateAroundLocal(body.quat,
				body.xWorld * 4,
				body.yWorld * 4,
				body.zWorld * 4
			);
			
			shader.setUniform("transformationMatrix", matrix);
		} else {
			Matrix4f matrix = new Matrix4f();
			matrix.translateLocal(
				x + body.xWorld * 4,
				y + body.yWorld * 4,
				z + body.zWorld * 4
			);
			
			if(body.staticFlags < -1) {
				matrix.rotateAroundLocal(body.quat,
					body.xWorld * 4,
					body.yWorld * 4,
					body.zWorld * 4
				);
			}
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
		
		mesh.render();
		
		dif.unbind();
		asg.unbind();
		nor.unbind();
	}
}