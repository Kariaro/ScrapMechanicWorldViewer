package com.hardcoded.lwjgl.cache;

import java.io.File;
import java.util.*;

import org.joml.Matrix4f;

import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hardcoded.logger.Log;
import com.hardcoded.lwjgl.WorldContentHandler;
import com.hardcoded.lwjgl.async.LwjglAsyncThread;
import com.hardcoded.lwjgl.mesh.Mesh;
import com.hardcoded.lwjgl.mesh.PartMesh;
import com.hardcoded.lwjgl.render.RenderPipeline.RenderObject;
import com.hardcoded.tile.object.Blueprint;
import com.hardcoded.util.FileUtils;
import com.hardcoded.util.ValueUtils;

/**
 * A blueprint cache.
 * 
 * @author HardCoded
 * @since v0.3
 */
public class WorldBlueprintCache implements WorldObjectCache {
	protected static final Log LOGGER = Log.getLogger();
	
	private final WorldContentHandler handler;
	private final String print;
	private boolean loaded;
	private String content;
	
	private List<BP_Object> list = new ArrayList<>();
	private BlueprintCache cache;
	
	public static class BlueprintCache {
		public Map<WorldBlockCache, List<RenderObject.Block>> blocks = new HashMap<>();
		public Map<WorldPartCache, List<RenderObject.Part>> parts = new HashMap<>();
	}
	
	public static class BlueprintObject {
		public Matrix4f modelMatrix;
		public int color;
		
		public BlueprintObject(Matrix4f modelMatrix) {
			this.modelMatrix = modelMatrix;
		}
	}
	
	public WorldBlueprintCache(WorldContentHandler handler, Blueprint blueprint) {
		this.handler = handler;
		
		String value = blueprint.getValue();
		if(value.isBlank()) {
			// This is some sort of error
			this.print = String.format("Blueprint@InvalidJSON@%08x", hashCode());
			return;
		}
		
		if(!blueprint.isLoaded()) {
			File file = handler.getContext().resolve(value);
			if(file != null) {
				content = FileUtils.readFile(file);
			}
			
			this.print = "Blueprint@" + value;
		} else {
			// Remove [?JB:]
			content = value.substring(4);
			this.print = String.format("Blueprint@DirectJson@%08x", hashCode());
		}
		
		// Load the blueprint string
		LwjglAsyncThread.runAsync(() -> {
			try {
				loadBlocks();
			} catch(Exception e) {
				e.printStackTrace();
			}
		});
	}
	
	private void loadBlocks() throws Exception {
		JsonFactory factory = new JsonFactory();
		JsonParser parser = factory.createParser(content);
		parser.nextToken();
		parser.nextToken();
		parser.nextToken();
		
		ObjectMapper mapper = new ObjectMapper();
		BP_Object[] objects = mapper.readValue(parser, BP_Object[].class);
		
		for(BP_Object obj : objects) {
			list.add(obj);
			
			System.out.println("body: " + obj);
			for(BodyChild shape : obj.childs) {
				System.out.println("    child: " + shape);
			}
		}
		
		tryLoadCache();
	}
	
	private void tryLoadCache() {
		boolean loaded = true;
		for(BP_Object obj : list) {
			for(BodyChild shape : obj.childs) {
				{
					WorldPartCache cache = handler.getPartCache(shape.shapeId);
					if(cache != null) {
						PartMesh part_mesh = cache.meshes.get(0);
						if(!part_mesh.isLoaded()) loaded = false;
					}
				}
			}
		}
		
		if(!loaded) {
			LwjglAsyncThread.runAsync(() -> {
				// LOGGER.warn("Failed to fully load blueprint. Waiting");
				tryLoadCache();
			});
			
			return;
		}
		
		BlueprintCache blueprint_cache = new BlueprintCache();
		for(BP_Object obj : list) {
			for(BodyChild shape : obj.childs) {
				{
					WorldBlockCache cache = handler.getBlockCache(shape.shapeId);
					if(cache != null) {
						List<RenderObject.Block> list = blueprint_cache.blocks.get(cache);
						if(list == null) {
							list = new ArrayList<>();
							blueprint_cache.blocks.put(cache, list);
						}
						
						Matrix4f modelMatrix = new Matrix4f()
							.scale(1 / 4.0f)
							.translate(shape.xPos, shape.yPos, shape.zPos);
						
						list.add(RenderObject.Block.get()
							.setColor(shape.color)
							.setTextures(List.of(cache.dif, cache.asg, cache.nor))
							.setModelMatrix(modelMatrix)
							
							// Block specific
							.setTiling(cache.block.tiling)
							.setLocalTransform(shape.xPos, shape.yPos, shape.zPos)
							.setScale(shape.xSize, shape.ySize, shape.zSize)
						);
					}
				}
				
				{
					WorldPartCache cache = handler.getPartCache(shape.shapeId);
					if(cache != null) {
						List<RenderObject.Part> list = blueprint_cache.parts.get(cache);
						if(list == null) {
							list = new ArrayList<>();
							blueprint_cache.parts.put(cache, list);
						}
						
						PartMesh part_mesh = cache.meshes.get(0);
						for(int i = 0; i < part_mesh.meshes.length; i++) {
							Mesh mesh = part_mesh.meshes[i];
							
							list.add(RenderObject.Part.get()
								.setVao(mesh.getVaoId())
								.setColor(shape.color)
								.setFlags(part_mesh.mats[i].getPipeFlags())
								.setTextures(part_mesh.textures[i])
								.setVertexCount(mesh.getVertexCount())
								.setModelMatrix(cache.calculateMatrix(shape))
							);
						}
					}
				}
			}
		}
		
		this.cache = blueprint_cache;
		this.loaded = true;
	}
	
	public List<BP_Object> getObjects() {
		return list;
	}
	
	public BlueprintCache getCache() {
		return cache;
	}
	
	public static class BP_Object {
		public Boolean restricted = false;
		public Boolean restricted_build = false;
		public Integer type = 0;
		public List<BodyChild> childs = List.of();
		
		@Override
		public String toString() {
			return String.format("Body [ restricted=%s, type=%s ]", restricted, type, childs);
		}
	}
	
	public static class BodyChild {
		public UUID shapeId;
		
		public int color;
		public int xPos;
		public int yPos;
		public int zPos;
		
		public int xaxis;
		public int zaxis;
		public int xSize;
		public int ySize;
		public int zSize;
		
		public Object joints;
		public Object controller;
		
		@JsonSetter(value = "color")
		private void setColor(String string) {
			try {
				int value = Integer.parseInt(string, 16);
				
				if(string.length() == 6) {
					this.color = (value << 8) | 0xff;
				} else {
					this.color = value;
				}
			} catch(NumberFormatException e) {
				
			}
		}
		
		@JsonSetter(value = "bounds")
		private void setBounds(Map<String, Object> map) {
			xSize = ValueUtils.toInt(map.get("x"));
			ySize = ValueUtils.toInt(map.get("y"));
			zSize = ValueUtils.toInt(map.get("z"));
		}
		
		@JsonSetter(value = "pos")
		private void set_pos(Map<String, Object> map) {
			xPos = ValueUtils.toInt(map.get("x"));
			yPos = ValueUtils.toInt(map.get("y"));
			zPos = ValueUtils.toInt(map.get("z"));
		}
		
		@JsonSetter(value = "xaxis")
		private void set_xaxis(Object value) {
			xaxis = ValueUtils.toInt(value, 1);
		}
		
		@JsonSetter(value = "zaxis")
		private void set_zaxis(Object value) {
			zaxis = ValueUtils.toInt(value, 1);
		}
		
		@Override
		public String toString() {
			return String.format("Child [ shapeId=%s, xaxis=%2d, zaxis=%2d, pos={ x: %3d, y: %3d, z: %3d }, bounds={ x: %3d, y: %3d, z: %3d }", shapeId, xaxis, zaxis, xPos, yPos, zPos, xSize, ySize, zSize);
		}
	}
	
	public boolean isLoaded() {
		return loaded;
	}
	
	@Override
	public String toString() {
		return print;
	}
}
