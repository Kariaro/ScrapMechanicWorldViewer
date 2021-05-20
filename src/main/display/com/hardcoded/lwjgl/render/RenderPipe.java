package com.hardcoded.lwjgl.render;

import com.hardcoded.lwjgl.WorldContentHandler;
import com.hardcoded.lwjgl.render.RenderPipeline.RenderObject;

/**
 * This abstract {@code RenderPipe} class is used to render objects to the render pipeline.
 * 
 * @author HardCoded
 * @since v0.3
 */
abstract class RenderPipe {
	protected final WorldContentHandler handler;
	protected final RenderPipeline pipeline;
	
	public RenderPipe(RenderPipeline pipeline) {
		this.pipeline = pipeline;
		this.handler = pipeline.handler;
	}
	
	protected final void push(RenderObject.Asset object) {
		pipeline.push(object);
	}
	
	protected final void push(RenderObject.Tile object) {
		pipeline.push(object);
	}
	
	protected final void push(RenderObject.Part object) {
		pipeline.push(object);
	}
	
	protected final void push(RenderObject.Block object) {
		pipeline.push(object);
	}
	
	/**
	 * This method is called when the world is reloaded
	 */
	public void onWorldReload() {}
	
	/**
	 * This method is called when the {@code RenderPipeline} class
	 * wants to push all objects to memory.
	 * 
	 * This is method is only called once per frame.
	 */
	public abstract void render();
}
