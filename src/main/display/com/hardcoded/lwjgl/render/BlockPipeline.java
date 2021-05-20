package com.hardcoded.lwjgl.render;

import java.util.ArrayList;
import java.util.List;

import com.hardcoded.game.World;
import com.hardcoded.lwjgl.cache.WorldBlockCache;
import com.hardcoded.lwjgl.cache.WorldPartCache;
import com.hardcoded.lwjgl.mesh.BlockMesh;
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
			
			// TODO: Sort blocks by their texture id
			for(RigidBody body : list) {
				for(ChildShape shape : body.shapes) {
					WorldBlockCache cache = handler.getBlockCache(shape.uuid);
					if(cache != null) {
						BlockMesh block_mesh = WorldBlockCache.mesh;
						
						blocks.add(RenderObject.Block.get()
							.setVao(block_mesh.getVaoId())
							.setColor(shape.colorRGBA)
							.setTextures(List.of(cache.dif, cache.asg, cache.nor))
							.setVertexCount(block_mesh.getVertexCount())
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
		
		for(RenderObject.Block object : blocks) {
			push(object);
		}
	}
}
