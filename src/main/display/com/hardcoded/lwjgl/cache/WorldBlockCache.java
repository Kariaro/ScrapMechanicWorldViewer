package com.hardcoded.lwjgl.cache;

import org.joml.Matrix4f;
import org.lwjgl.opengl.GL20;

import com.hardcoded.asset.ScrapMechanicAssetHandler;
import com.hardcoded.db.types.SMBlock;
import com.hardcoded.lwjgl.data.Texture;
import com.hardcoded.lwjgl.mesh.BlockMesh;
import com.hardcoded.sm.objects.BodyList.ChildShape;
import com.hardcoded.sm.objects.BodyList.RigidBody;

/**
 * A block cache.
 * 
 * @author HardCoded
 * @since v0.1
 * 
 * TODO: Transparency
 */
public class WorldBlockCache implements WorldObjectCache {
	/**
	 * This field is instantiated from inside {@code WorldRender}
	 * 
	 * This is because this field needs to be effectivly loaded on
	 * the gl thread but really early to prevent null checking
	 */
	public static BlockMesh mesh;
	public final SMBlock block;
	public final Texture dif;
	public final Texture asg;
	public final Texture nor;
	
	public WorldBlockCache(SMBlock block) {
		this.block = block;
		
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
	
	public Matrix4f calculateMatrix(ChildShape shape) {
		float x = shape.xPos;
		float y = shape.yPos;
		float z = shape.zPos;
		
		RigidBody body = shape.body;

		Matrix4f matrix = new Matrix4f();
		
		{
			if(shape.body.isGridLocked_0_2 == 2) {
				matrix.rotate(body.quat);
			} else {
				if(body.staticFlags < -1) {
					matrix.rotate(body.quat);
				}
			}
			matrix.translateLocal(body.xWorld, body.yWorld, body.zWorld);
			matrix.scale(1 / 4.0f);
			matrix.translate(x, y, z);
		}
		
		return matrix;
	}
}
