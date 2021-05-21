package com.hardcoded.lwjgl.render;

import java.util.ArrayList;
import java.util.List;

import org.joml.Vector3f;

import com.hardcoded.game.World;
import com.hardcoded.lwjgl.cache.WorldBlockCache;
import com.hardcoded.lwjgl.cache.WorldPartCache;
import com.hardcoded.lwjgl.mesh.PartMesh;
import com.hardcoded.lwjgl.render.RenderPipeline.RenderObject;
import com.hardcoded.sm.objects.BodyList.ChildShape;
import com.hardcoded.sm.objects.BodyList.RigidBody;

/**
 * This is a block pipeline class.
 * 
 * @author HardCoded
 * @since v0.3
 */
public class BlockPipeline extends RenderPipe {
	public BlockPipeline(RenderPipeline pipeline) {
		super(pipeline);
	}
	
	private List<RenderObject.Block> blocks = new ArrayList<>();
	private boolean reload_cache = false;
	
	@Override
	public void onWorldReload() {
		reload_cache = true;
		blocks.clear();
	}
	
	@Override
	public void render() {
		if(reload_cache) {
			World world = pipeline.getWorld();
			List<RigidBody> list = world.getRigidBodies();
			
			boolean loaded = true;
			for(RigidBody body : list) {
				for(ChildShape shape : body.shapes) {
					WorldPartCache cache = handler.getPartCache(shape.uuid);
					if(cache != null) {
						PartMesh part_mesh = cache.meshes.get(0);
						if(!part_mesh.isLoaded()) {
							loaded = false;
						}
					}
				}
			}
			
			if(!loaded) {
				return;
			}
			
			this.blocks = new ArrayList<>();
			
			for(RigidBody body : list) {
				for(ChildShape shape : body.shapes) {
					WorldBlockCache cache = handler.getBlockCache(shape.uuid);
					if(cache != null) {
						blocks.add(RenderObject.Block.get()
							.setVao(cache.hashCode()) // Set the id to the hashCode of the object. This will make sure some blocks are unique
							.setColor(shape.colorRGBA)
							.setTextures(List.of(cache.dif, cache.asg, cache.nor))
							.setModelMatrix(cache.calculateMatrix(shape))
							
							// Block specific
							.setTiling(cache.block.tiling)
							.setLocalTransform(shape.xPos, shape.yPos, shape.zPos)
							.setScale(shape.xSize, shape.ySize, shape.zSize)
						);
					}
				}
			}
			
			reload_cache = false;
		}
		
		Vector3f camera = pipeline.getCamera().getPosition();
		for(RenderObject.Block object : blocks) {
			float dist = object.modelMatrix.transformPosition(new Vector3f()).distance(camera);
			if(dist < 100) {
				push(object);
			}
		}
	}
}
