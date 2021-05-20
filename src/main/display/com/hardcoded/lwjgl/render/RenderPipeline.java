package com.hardcoded.lwjgl.render;

import java.util.*;

import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.lwjgl.opengl.*;

import com.hardcoded.game.World;
import com.hardcoded.logger.Log;
import com.hardcoded.lwjgl.Camera;
import com.hardcoded.lwjgl.WorldContentHandler;
import com.hardcoded.lwjgl.data.Texture;
import com.hardcoded.lwjgl.shader.*;
import com.hardcoded.lwjgl.shadow.ShadowFrameBuffer;
import com.hardcoded.lwjgl.shadow.ShadowShader;

/**
 * This class is the rendering pipe line for all the models
 * 
 * @author HardCoded
 * @since v0.3
 */
public class RenderPipeline {
	protected static final Log LOGGER = Log.getLogger();
	
	protected final WorldContentHandler handler;
	private final List<RenderPipe> pipelines;
	
	protected Camera camera;
	protected World world;
	
	protected Matrix4f viewMatrix;
	protected Matrix4f projectionView;
	protected Matrix4f mvpMatrix;
	protected Matrix4f toShadowMapMatrix;
	
	protected ShadowFrameBuffer frameBuffer;
	protected ShadowShader shadowShader;
	protected AssetShader assetShader;
	protected BlockShader blockShader;
	protected TileShader tileShader;
	protected PartShader partShader;
	
	public RenderPipeline(WorldContentHandler handler, Camera camera) {
		this.pipelines = new ArrayList<>();
		this.handler = handler;
		this.camera = camera;
	}
	
	public void init() {
		// Shadows
		this.shadowShader = handler.shadowShader;
		this.frameBuffer = handler.frameBuffer;
		
		// Renders
		this.assetShader = handler.assetShader;
		this.blockShader = handler.blockShader;
		this.tileShader = handler.tileShader;
		this.partShader = handler.partShader;
	}
	
	public void loadPipelines() {
		pipelines.add(new TilePipeline(this));
		//pipelines.add(new PartPipeline(this));
		//pipelines.add(new BlockPipeline(this));
	}
	
	public void cleanUp() {
		
	}
	
	/**
	 * Returns the current camera.
	 * @return the current camera
	 */
	public Camera getCamera() {
		return camera;
	}
	
	/**
	 * Returns the current viewMatrix.
	 * @return the current viewMatrix
	 */
	public Matrix4f getViewMatrix() {
		return viewMatrix;
	}
	
	/**
	 * Returns the current projectionView matrix.
	 * @return the current projectionView matrix
	 */
	public Matrix4f getProjectionView() {
		return projectionView;
	}
	
	/**
	 * Returns the current shadowMap matrix.
	 * @return the current shadowMap matrix
	 */
	public Matrix4f getShadowMapMatrix() {
		return toShadowMapMatrix;
	}
	
	/**
	 * Returns the current loaded world.
	 * @return the current loaded world
	 */
	public World getWorld() {
		return world;
	}
	
	public void load(Matrix4f mvpMatrix, Matrix4f viewMatrix, Matrix4f projectionView, Matrix4f toShadowMapMatrix) {
		this.mvpMatrix = mvpMatrix;
		this.viewMatrix = viewMatrix;
		this.projectionView = projectionView;
		this.toShadowMapMatrix = toShadowMapMatrix;
	}
	
	public void loadWorld(World world) {
		this.world = world;
		
		for(RenderPipe pipe : pipelines) {
			pipe.onWorldReload();
		}
	}
	
	private Map<Integer, List<RenderObject.Asset>> pushAssetShader = new HashMap<>();
	private Map<Integer, List<RenderObject.Block>> pushBlockShader = new HashMap<>();
	private Map<Integer, List<RenderObject.Tile>> pushTileShader = new HashMap<>();
	private Map<Integer, List<RenderObject.Part>> pushPartShader = new HashMap<>();
	
	protected void push(RenderObject.Asset object) {
		List<RenderObject.Asset> list = pushAssetShader.get(object.vao);
		if(list == null) {
			list = new ArrayList<>();
			pushAssetShader.put(object.vao, list);
		}
		
		list.add(object);
	}
	
	protected void push(RenderObject.Tile object) {
		List<RenderObject.Tile> list = pushTileShader.get(object.vao);
		if(list == null) {
			list = new ArrayList<>();
			pushTileShader.put(object.vao, list);
		}
		
		list.add(object);
	}
	
	protected void push(RenderObject.Part object) {
		List<RenderObject.Part> list = pushPartShader.get(object.vao);
		if(list == null) {
			list = new ArrayList<>();
			pushPartShader.put(object.vao, list);
		}
		
		list.add(object);
	}
	
	protected void push(RenderObject.Block object) {
		List<RenderObject.Block> list = pushBlockShader.get(object.vao);
		if(list == null) {
			list = new ArrayList<>();
			pushBlockShader.put(object.vao, list);
		}
		
		list.add(object);
	}
	
	
	private final Texture[] loaded = new Texture[32];
	private void bind(List<Texture> textures) {
		final int len = textures.size();
		for(int i = 0; i < len; i++) {
			final Texture tex = textures.get(i);
			if(tex == Texture.NONE) {
				if(loaded[i] != null) {
					loaded[i].unbind();
					loaded[i] = null;
				}
				continue;
			}
			
			final int activeId = tex.activeId - GL13.GL_TEXTURE0;
			final Texture lod = loaded[activeId];
			if(lod == null) {
				loaded[activeId] = tex;
				tex.bind();
			} else if(lod.textureId != tex.textureId) {
				lod.unbind();
				loaded[activeId] = tex;
				tex.bind();
			}
		}
	}
	
	private void unbind() {
		for(int i = 0; i < loaded.length; i++) {
			final Texture tex = loaded[i];
			if(tex != null) {
				tex.unbind();
				loaded[i] = null;
			}
		}
		
		// Set all flags to zero
		bindFlags(0);
	}
	
	public static final int PIPE_CULL_FACE	= 0x1;
	public static final int PIPE_ALPHA		= 0x2;
	public static final int PIPE_WAVE		= 0x4;
	
	// Texture bits
	public static final int PIPE_ASG_TEX	= 0x8;
	public static final int PIPE_NOR_TEX	= 0x10;
	public static final int PIPE_AO_TEX		= 0x20;
	
	private Shader current_shader;
	
	private int last_flags = 0;
	@SuppressWarnings("deprecation")
	private void bindFlags(int flags) {
		int mask = last_flags ^ flags;
		if((mask & PIPE_CULL_FACE) != 0) bindFlagBit(flags & PIPE_CULL_FACE, GL11.GL_CULL_FACE);
		if((mask & PIPE_ALPHA    ) != 0) {
			final int val = flags & PIPE_ALPHA;
			bindFlagBit(val, GL11.GL_BLEND);
			current_shader.setUniform("hasAlpha", val != 0);
		}
		this.last_flags = flags;
	}
	
	private void bindFlagBit(int value, int gl) {
		if(value == 0)
			GL11.glDisable(gl);
		else
			GL11.glEnable(gl);
	}
	
	
	/**
	 * This method resets the pipeline and rerenders all the screen objects.
	 */
	private void renderPipes() {
		pushAssetShader.clear();
		pushBlockShader.clear();
		pushTileShader.clear();
		pushPartShader.clear();
		
		for(int i = 0; i < loaded.length; i++) {
			loaded[i] = null;
		}
		
		for(RenderPipe pipe : pipelines) {
			pipe.render();
		}
		
//		LOGGER.info("Pushed vao assets: %d", pushAssetShader.size());
//		LOGGER.info("Pushed vao tiles: %d", pushTileShader.size());
//		LOGGER.info("Pushed vao parts: %d", pushPartShader.size());
//		LOGGER.info();
		
		// Setup fields
		GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
	}

	private int last_mvp_x = Integer.MAX_VALUE;
	private int last_mvp_y = Integer.MIN_VALUE;
	private void renderShadows() {
		int mvp_x = -(int)(camera.x / 64);
		int mvp_y = -(int)(camera.y / 64);
		if(last_mvp_x == mvp_x && last_mvp_y == mvp_y) return;
		
		last_mvp_x = mvp_x;
		last_mvp_y = mvp_y;
		
		frameBuffer.bindFrameBuffer();
		GL11.glClear(GL11.GL_COLOR_BUFFER_BIT);
		GL11.glClear(GL11.GL_DEPTH_BUFFER_BIT);
		GL11.glEnable(GL11.GL_DEPTH_TEST);
		
		shadowShader.bind();
		shadowShader.setMvpMatrix(mvpMatrix);
		current_shader = shadowShader;
		
		{
			for(int vaoId : pushTileShader.keySet()) {
				List<RenderObject.Tile> list = pushTileShader.get(vaoId);
				
				GL30.glBindVertexArray(vaoId);
				GL20.glEnableVertexAttribArray(0);
				
				for(RenderObject.Tile rend : list) {
					shadowShader.setMvpMatrix(new Matrix4f(mvpMatrix).mul(rend.modelMatrix));
					GL11.glDrawArrays(GL11.GL_TRIANGLES, 0, rend.vertexCount);
				}
				
				GL20.glDisableVertexAttribArray(0);
				GL30.glBindVertexArray(0);
			}
		}
		
		{
			for(int vaoId : pushAssetShader.keySet()) {
				List<RenderObject.Asset> list = pushAssetShader.get(vaoId);
				
				GL30.glBindVertexArray(vaoId);
				GL20.glEnableVertexAttribArray(0);
				
				for(RenderObject.Asset rend : list) {
					shadowShader.setMvpMatrix(new Matrix4f(mvpMatrix).mul(rend.modelMatrix));
					bindFlags(rend.flags);
					GL11.glDrawElements(GL11.GL_TRIANGLES, rend.vertexCount, GL11.GL_UNSIGNED_INT, 0);
				}
				
				GL20.glDisableVertexAttribArray(0);
				GL30.glBindVertexArray(0);
			}
		}
		
		{
			for(int vaoId : pushPartShader.keySet()) {
				List<RenderObject.Part> list = pushPartShader.get(vaoId);
				
				GL30.glBindVertexArray(vaoId);
				GL20.glEnableVertexAttribArray(0);
				
				for(RenderObject.Part rend : list) {
					shadowShader.setMvpMatrix(new Matrix4f(mvpMatrix).mul(rend.modelMatrix));
					bindFlags(rend.flags);
					GL11.glDrawElements(GL11.GL_TRIANGLES, rend.vertexCount, GL11.GL_UNSIGNED_INT, 0);
				}
				
				GL20.glDisableVertexAttribArray(0);
				GL30.glBindVertexArray(0);
			}
		}
		
		{
			for(int vaoId : pushBlockShader.keySet()) {
				List<RenderObject.Block> list = pushBlockShader.get(vaoId);
				
				GL30.glBindVertexArray(vaoId);
				GL20.glEnableVertexAttribArray(0);
				GL20.glEnableVertexAttribArray(1);
				GL20.glEnableVertexAttribArray(2);
				
				for(RenderObject.Block rend : list) {
					shadowShader.setMvpMatrix(new Matrix4f(mvpMatrix).mul(rend.modelMatrix.scale(rend.scale, new Matrix4f())));
					bindFlags(rend.flags);
					GL11.glDrawArrays(GL11.GL_TRIANGLES, 0, rend.vertexCount);
				}
				
				GL20.glDisableVertexAttribArray(0);
				GL20.glDisableVertexAttribArray(1);
				GL20.glDisableVertexAttribArray(2);
				GL30.glBindVertexArray(0);
			}
		}
		
		shadowShader.unbind();
		frameBuffer.unbindFrameBuffer();
	}
	
	public void render() {
		// Render the pipelines
		renderPipes();
		
		// Apply shadows
		renderShadows();
		{
			GL13.glActiveTexture(GL13.GL_TEXTURE9);
			GL11.glBindTexture(GL11.GL_TEXTURE_2D, frameBuffer.getShadowMap());
		}

		@SuppressWarnings("unused")
		int renders = 0, polys = 0;
		
		// Render scene
		{
			current_shader = tileShader;
			tileShader.bind();
			tileShader.setProjectionView(projectionView);
			tileShader.setViewMatrix(viewMatrix);
			tileShader.setShadowMapSpace(toShadowMapMatrix);
			
			final int textures = TileShader.textures.length;
			for(int i = 0; i < textures; i++) TileShader.textures[i].bind();
			
			for(int vaoId : pushTileShader.keySet()) {
				List<RenderObject.Tile> list = pushTileShader.get(vaoId);
				
				GL30.glBindVertexArray(vaoId);
				GL20.glEnableVertexAttribArray(0);
				GL20.glEnableVertexAttribArray(1);
				GL20.glEnableVertexAttribArray(2);
				GL20.glEnableVertexAttribArray(3);
				GL20.glEnableVertexAttribArray(4);
				GL20.glEnableVertexAttribArray(5);
				GL20.glEnableVertexAttribArray(6);
				
				for(RenderObject.Tile rend : list) {
					tileShader.setModelMatrix(rend.modelMatrix);
					GL11.glDrawArrays(GL11.GL_TRIANGLES, 0, rend.vertexCount);
					renders++;
					polys += rend.vertexCount;
				}
				
				GL20.glDisableVertexAttribArray(0);
				GL20.glDisableVertexAttribArray(1);
				GL20.glDisableVertexAttribArray(2);
				GL20.glDisableVertexAttribArray(3);
				GL20.glDisableVertexAttribArray(4);
				GL20.glDisableVertexAttribArray(5);
				GL20.glDisableVertexAttribArray(6);
				GL30.glBindVertexArray(0);
			}
			
			for(int i = 0; i < textures; i++) TileShader.textures[i].unbind();
			
			tileShader.unbind();
		}
		
		{
			current_shader = assetShader;
			assetShader.bind();
			assetShader.setProjectionView(projectionView);
			assetShader.setViewMatrix(viewMatrix);
			assetShader.setShadowMapSpace(toShadowMapMatrix);
			
			for(int vaoId : pushAssetShader.keySet()) {
				List<RenderObject.Asset> list = pushAssetShader.get(vaoId);
				
				GL30.glBindVertexArray(vaoId);
				GL20.glEnableVertexAttribArray(0);
				GL20.glEnableVertexAttribArray(1);
				GL20.glEnableVertexAttribArray(2);
				GL20.glEnableVertexAttribArray(3);
				
				for(RenderObject.Asset rend : list) {
					assetShader.setModelMatrix(rend.modelMatrix);
					if(rend.color != null)
						assetShader.setColor(rend.color);
					
					bindFlags(rend.flags);
					bind(rend.textures);
					GL11.glDrawElements(GL11.GL_TRIANGLES, rend.vertexCount, GL11.GL_UNSIGNED_INT, 0);
					renders++;
					polys += rend.vertexCount;
				}
				
				GL20.glDisableVertexAttribArray(0);
				GL20.glDisableVertexAttribArray(1);
				GL20.glDisableVertexAttribArray(2);
				GL20.glDisableVertexAttribArray(3);
				GL30.glBindVertexArray(0);
			}
			
			assetShader.unbind();
		}
		
		{
			current_shader = partShader;
			partShader.bind();
			partShader.setProjectionView(projectionView);
			partShader.setViewMatrix(viewMatrix);
			partShader.setShadowMapSpace(toShadowMapMatrix);
			
			for(int vaoId : pushPartShader.keySet()) {
				List<RenderObject.Part> list = pushPartShader.get(vaoId);
				
				GL30.glBindVertexArray(vaoId);
				GL20.glEnableVertexAttribArray(0);
				GL20.glEnableVertexAttribArray(1);
				GL20.glEnableVertexAttribArray(2);
				GL20.glEnableVertexAttribArray(3);
				
				for(RenderObject.Part rend : list) {
					partShader.setModelMatrix(rend.modelMatrix);
					if(rend.color != null)
						partShader.setColor(rend.color);
					
					bindFlags(rend.flags);
					bind(rend.textures);
					GL11.glDrawElements(GL11.GL_TRIANGLES, rend.vertexCount, GL11.GL_UNSIGNED_INT, 0);
					renders++;
					polys += rend.vertexCount;
				}
				
				GL20.glDisableVertexAttribArray(0);
				GL20.glDisableVertexAttribArray(1);
				GL20.glDisableVertexAttribArray(2);
				GL20.glDisableVertexAttribArray(3);
				GL30.glBindVertexArray(0);
			}
			
			partShader.unbind();
		}
		
		{
			current_shader = blockShader;
			blockShader.bind();
			blockShader.setProjectionView(projectionView);
			blockShader.setViewMatrix(viewMatrix);
			blockShader.setShadowMapSpace(toShadowMapMatrix);
			
			for(int vaoId : pushBlockShader.keySet()) {
				List<RenderObject.Block> list = pushBlockShader.get(vaoId);
				
				GL30.glBindVertexArray(vaoId);
				GL20.glEnableVertexAttribArray(0);
				GL20.glEnableVertexAttribArray(1);
				GL20.glEnableVertexAttribArray(2);
				
				for(RenderObject.Block rend : list) {
					blockShader.setModelMatrix(rend.modelMatrix);
					if(rend.color != null)
						blockShader.setColor(rend.color);
					
					blockShader.setLocalTransform(rend.localTransform);
					blockShader.setScale(rend.scale);
					blockShader.setTiling(rend.tiling);
					
					bindFlags(rend.flags);
					bind(rend.textures);
					GL11.glDrawArrays(GL11.GL_TRIANGLES, 0, rend.vertexCount);
					renders++;
					polys += rend.vertexCount;
				}
				
				GL20.glDisableVertexAttribArray(0);
				GL20.glDisableVertexAttribArray(1);
				GL20.glDisableVertexAttribArray(2);
				GL30.glBindVertexArray(0);
			}
			
			blockShader.unbind();
		}
		
		unbind();
		
//		LOGGER.info("Total drawn objects: %d", renders);
//		LOGGER.info("Total drawn triangels: %d", polys / 3);
	}
	
	/**
	 * This class is used to create objects that are pushed onto the render pipeline.
	 * 
	 * @author HardCoded
	 * @since v0.3
	 *
	 * @param <T> the shader type
	 */
	@SuppressWarnings("unchecked")
	public static abstract class RenderObject<T extends RenderObject<T>> {
		Matrix4f modelMatrix;
		Vector4f color;
		List<Texture> textures;
		int vertexCount;
		int flags;
		int vao;
		
		public static class Tile extends RenderObject<Tile> {
			public static Tile get() {
				return new Tile();
			}
		}
		
		public static class Asset extends RenderObject<Asset> {
			public static Asset get() {
				return new Asset();
			}
		}
		
		public static class Part extends RenderObject<Part> {
			public static Part get() {
				return new Part();
			}
		}
		
		public static class Block extends RenderObject<Block> {
			public Vector3f localTransform;
			public Vector3f scale;
			public int tiling;
			
			public static Block get() {
				return new Block();
			}
			
			public Block setLocalTransform(float x, float y, float z) {
				this.localTransform = new Vector3f(x, y, z);
				return this;
			}
			
			public Block setLocalTransform(Vector3f localTransform) {
				if(localTransform != null) {
					this.localTransform = localTransform.get(new Vector3f());
				}
				
				return this;
			}
			
			public Block setScale(float x, float y, float z) {
				this.scale = new Vector3f(x, y, z);
				return this;
			}
			
			public Block setScale(Vector3f scale) {
				if(scale != null) {
					this.scale = scale.get(new Vector3f());
				}
				
				return this;
			}
			
			public Block setTiling(int tiling) {
				this.tiling = tiling;
				return this;
			}
		}
		
		public T setModelMatrix(Matrix4f modelMatrix) {
			this.modelMatrix = modelMatrix;
			return (T)this;
		}
		
		public T setVao(int vao) {
			this.vao = vao;
			return (T)this;
		}
		
		public T setFlags(int flags) {
			this.flags = flags;
			return (T)this;
		}
		
		public T setVertexCount(int vertexCount) {
			this.vertexCount = vertexCount;
			return (T)this;
		}
		
		public T setTextures(List<Texture> list) {
			this.textures = list;
			return (T)this;
		}
		
		public T setColor(Integer color) {
			if(color != null) {
				this.color = new Vector4f(
					((color >> 24) & 0xff) / 255.0f,
					((color >> 16) & 0xff) / 255.0f,
					((color >>  8) & 0xff) / 255.0f,
					((color >>  0) & 0xff) / 255.0f
				);
			}
			
			return (T)this;
		}
		
		public T setColor(Vector4f color) {
			if(color != null) {
				this.color = color.get(new Vector4f());
			}
			
			return (T)this;
		}
		
		public T setColor(Integer color, int[] defaults, int index) {
			if(color != null) {
				this.color = new Vector4f(
					((color >> 24) & 0xff) / 255.0f,
					((color >> 16) & 0xff) / 255.0f,
					((color >>  8) & 0xff) / 255.0f,
					((color >>  0) & 0xff) / 255.0f
				);
			} else if(defaults != null && defaults.length > 0) {
				if(index >= defaults.length) {
					color = defaults[0];
				} else {
					color = defaults[index];
				}
				this.color = new Vector4f(
					((color >> 24) & 0xff) / 255.0f,
					((color >> 16) & 0xff) / 255.0f,
					((color >>  8) & 0xff) / 255.0f,
					((color >>  0) & 0xff) / 255.0f
				);
			}
			
			return (T)this;
		}
	}
}
