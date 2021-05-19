package com.hardcoded.lwjgl.meshrender;

import com.hardcoded.lwjgl.WorldContentHandler;
import com.hardcoded.lwjgl.meshrender.RenderPipeline.RenderObject;

/**
 * A render pipe class.
 * 
 * @author HardCoded
 * @since v0.3
 */
public abstract class RenderPipe {
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
	
	public abstract void render();
}
