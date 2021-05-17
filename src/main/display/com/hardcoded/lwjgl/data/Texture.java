package com.hardcoded.lwjgl.data;

import static org.lwjgl.opengl.GL11.*;

import java.awt.Transparency;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;

import javax.imageio.ImageIO;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;
import org.lwjgl.opengl.GL20;
import org.lwjgl.stb.STBImage;

import com.hardcoded.logger.Log;
import com.hardcoded.lwjgl.async.LwjglAsyncThread;

/**
 * A texture class.
 * 
 * @author HardCoded
 * @since v0.1
 * 
 * TODO: Clear this class of old unused fields
 */
public class Texture {
	private static final Log LOGGER = Log.getLogger();
	@SuppressWarnings("unused")
	private static final Map<String, Texture> cache = new HashMap<>();
	
	public static final Texture NONE = new Texture();
	public static final Map<String, Integer> cacheTextureId = new HashMap<>();
	
	private final String path;
	public final String name;
	public final int activeId;
	public final int textureId;
	public final int interpolation;
	public int height;
	public int width;
	
	private Texture() {
		name = null;
		path = null;
		interpolation = -1;
		activeId = -1;
		textureId = -1;
	}
	
	private Texture(File file, String path, int id, int interpolation) throws IOException {
		this.activeId = GL20.GL_TEXTURE0 + id;
		this.textureId = GL11.glGenTextures();
		this.interpolation = interpolation;
		this.name = path;
		this.path = path;
		
		//load(buf, interpolation);
		
		cacheTextureId.put(path, this.textureId);
		
		if(!LwjglAsyncThread.isCurrentThread()) {
			LwjglAsyncThread.runAsync(this::loadData);
			return;
		}
		
		loadData();
	}
	
	private Texture(int textureId, int activeId) {
		this.textureId = textureId;
		this.activeId = GL20.GL_TEXTURE0 + activeId;
		this.name = null;
		this.path = null;
		this.interpolation = -1;
	}
	
	private Texture(BufferedImage bi, int id, int interpolation) {
		this.activeId = GL20.GL_TEXTURE0 + id;
		this.textureId = GL11.glGenTextures();
		this.interpolation = interpolation;
		this.name = bi.toString();
		this.path = null;
		
		ByteBuffer buf = loadBuffer(bi, false);
		this.height = bi.getHeight();
		this.width = bi.getWidth();
		
		load(buf, interpolation);
	}
	
	private void load(ByteBuffer buf, int interpolation) {
		GL20.glActiveTexture(this.activeId);
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, textureId);
		GL11.glPixelStorei(GL11.GL_UNPACK_ALIGNMENT, 1);
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL12.GL_REPEAT);
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL12.GL_REPEAT);
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, interpolation);
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, interpolation);
		GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA, width, height, 0, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, buf);
	}
	
	private void loadData() {
		ByteBuffer buf = null;
//		try {
//			if(path.toLowerCase().endsWith(".tga")) {
			int[] w = new int[1];
			int[] h = new int[1];
			int[] channels = new int[1];
			buf = STBImage.stbi_load(path, h, w, channels, 4);
			height = w[0];
			width = h[0];
			//bi = null;
//			}
//			else {
//				BufferedImage bi = ImageIO.read(new File(path));
//				buf = loadBuffer(bi, false);
//				height = bi.getHeight();
//				width = bi.getWidth();
//			}
//		}
		
		GL20.glActiveTexture(this.activeId);
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, textureId);
		GL11.glPixelStorei(GL11.GL_UNPACK_ALIGNMENT, 1);
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL12.GL_REPEAT);
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL12.GL_REPEAT);
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, interpolation);
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, interpolation);
		GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA, width, height, 0, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, buf);
	}
	
	public void bind() {
		if(this == NONE) return;
		GL20.glActiveTexture(activeId);
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, textureId);
	}
	
	public void unbind() {
		if(this == NONE) return;
		GL20.glActiveTexture(activeId);
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0);
	}

	public void cleanup() {
		if(this == NONE) return;
		glDeleteTextures(textureId);
	}
	
	@Override
	public String toString() {
		if(this == NONE) return "Texture[none]";
		return new StringBuilder().append("Texture[id=").append(textureId).append("] (").append(width).append("x").append(height).append(")").toString();
	}
	
	public static Texture loadTexture(File file, int activeId, int interpolation) throws IOException {
		return loadTexture0(file, file.getAbsolutePath(), activeId, interpolation);
	}
	
	public static Texture loadTexture(String path, int activeId, int interpolation) throws IOException {
		return loadTexture0(new File(path), path, activeId, interpolation);
	}
	
	private static Texture loadTexture0(File file, String path, int activeId, int interpolation) throws IOException {
		if(!file.exists()) {
			// LOGGER.warn("Tried to load texture '" + path + "' but that file does not exist");
			return NONE;
		}
		
		Texture tex = cache.get(path);
		if(tex == null) {
			tex = new Texture(file, path, activeId, interpolation);
			cache.put(path, tex);
			return new Texture(cacheTextureId.get(path), activeId);
		}
		
		return new Texture(tex.textureId, activeId);
	}
	
	public static Texture loadResourceTexture(String path, int interpolation) throws IOException, URISyntaxException {
		File file = new File(Texture.class.getResource(path).toURI());
		
		String pathCheck = file.getAbsolutePath();
		if(cacheTextureId.containsKey(pathCheck)) {
			return new Texture(cacheTextureId.get(pathCheck), 0);
		}
		
		return new Texture(file, path, 0, interpolation);
	}
	
	public static Texture loadBufferedImageTexture(BufferedImage bi, int interpolation) {
		return new Texture(bi, 0, interpolation);
	}
	
	public static ByteBuffer loadBuffer(BufferedImage bi) {
		return loadBuffer(bi, false);
	}
	
	private static ByteBuffer loadBuffer(BufferedImage bi, boolean flip_vertical) {
		if(bi == null) return null;
		
		int height = bi.getHeight();
		int width = bi.getWidth();
		
		boolean alpha = bi.getTransparency() == Transparency.TRANSLUCENT;
		
		ByteBuffer buf = ByteBuffer.allocateDirect(4 * width * height);
		int[] pixels = new int[width * height];
		bi.getRGB(0, 0, width, height, pixels, 0, width);
		for(int i = 0; i < height; i++) {
			for(int j = 0; j < width; j++) {
				int pos;
				if(flip_vertical) {
					pos = (height - i - 1) * width + j;
				} else {
					pos = i * width + j;
				}
				
				int pixel = pixels[pos];
				int r = (pixel >> 16) & 0xff;
				int g = (pixel >>  8) & 0xff;
				int b = (pixel      ) & 0xff;
				
				buf.put((byte)r);
				buf.put((byte)g);
				buf.put((byte)b);
				if(alpha) {
					buf.put((byte)((pixel >> 24) & 0xff));
				} else {
					buf.put((byte)0xff);
				}
			}
		}
		buf.flip();
		
		return buf;
	}
}