package sm.lwjgl.mesh;

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

public class Texture {
	public static final Texture NONE = new Texture();
	private static final Map<String, Texture> cache = new HashMap<>();
	public final BufferedImage bi;
	public final int activeId;
	public final int textureId;
	public final int height;
	public final int width;
	
	private Texture() {
		bi = null;
		activeId = -1;
		textureId = -1;
		height = -1;
		width = -1;
	}
	
	private Texture(File file, int id, int interpolation) throws IOException {
		this.activeId = GL20.GL_TEXTURE0 + id;
		String path = file.getAbsolutePath();
		
		ByteBuffer buf;
		if(file.getName().toLowerCase().endsWith(".tga")) {
			int[] w = new int[1];
			int[] h = new int[1];
			buf = org.lwjgl.stb.STBImage.stbi_load(path, w, h, new int[] { 0 }, 4);
			height = w[0];
			width = h[0];
			bi = null;
		} else {
			bi = ImageIO.read(file);
			System.out.println(file);
			buf = loadBuffer(bi, true);
			height = bi.getHeight();
			width = bi.getWidth();
		}
		
		textureId = GL11.glGenTextures();
		load(buf, interpolation);
		
		cache.put(path, this);
	}
	
	private void load(ByteBuffer buf, int interpolation) {
		GL11.glBindTexture(GL_TEXTURE_2D, textureId);
		GL11.glPixelStorei(GL_UNPACK_ALIGNMENT, 1);
		GL11.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL12.GL_REPEAT);
		GL11.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL12.GL_REPEAT);
		GL11.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, interpolation);
		GL11.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, interpolation);
		GL11.glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, width, height, 0, GL_RGBA, GL_UNSIGNED_BYTE, buf);
	}
	
	public void bind() {
		if(this == NONE) return;
		GL20.glActiveTexture(activeId);
		GL11.glBindTexture(GL_TEXTURE_2D, textureId);
	}
	
	public void unbind() {
		if(this == NONE) return;
		GL20.glActiveTexture(activeId);
		GL11.glBindTexture(GL_TEXTURE_2D, 0);
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
	
	public static Texture loadTexture(String path, int textureId, int interpolation) throws IOException {
		File file = new File(path);
		
		String pathCheck = file.getAbsolutePath();
		if(cache.containsKey(pathCheck)) {
			return cache.get(pathCheck);
		}
		
		if(!file.exists()) {
			return NONE;
		}
		
		return new Texture(file, textureId, interpolation);
	}
	
	public static Texture loadResourceTexture(String path, int interpolation) throws IOException, URISyntaxException {
		File file = new File(Texture.class.getResource(path).toURI());
		
		String pathCheck = file.getAbsolutePath();
		if(cache.containsKey(pathCheck)) {
			return cache.get(pathCheck);
		}
		
		return new Texture(file, 0, interpolation);
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