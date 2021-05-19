package com.hardcoded.lwjgl.meshrender;

import java.util.ArrayList;
import java.util.List;

import com.hardcoded.game.World;
import com.hardcoded.lwjgl.mesh.BlockMesh;
import com.hardcoded.lwjgl.mesh.PartMesh;
import com.hardcoded.lwjgl.meshrender.RenderPipeline.RenderObject;
import com.hardcoded.lwjgl.render.WorldBlockRender;
import com.hardcoded.lwjgl.render.WorldPartRender;
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
					WorldPartRender rend = handler.getPartRender(shape.uuid);
					if(rend != null) {
						PartMesh part_mesh = rend.meshes.get(0);
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
					WorldBlockRender rend = handler.getBlockRender(shape.uuid);
					if(rend != null) {
						BlockMesh block_mesh = WorldBlockRender.mesh;
						
						blocks.add(RenderObject.Block.get()
							.setVao(block_mesh.getVaoId())
							.setColor(shape.colorRGBA)
							.setTextures(List.of(rend.dif, rend.asg, rend.nor))
							.setVertexCount(block_mesh.getVertexCount())
							.setModelMatrix(rend.calculateMatrix(shape))
							
							// Block specific
							.setTiling(rend.block.tiling)
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
