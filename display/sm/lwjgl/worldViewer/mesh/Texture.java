package sm.lwjgl.worldViewer.mesh;

import static org.lwjgl.opengl.GL11.*;

import java.awt.Transparency;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import java.nio.ByteBuffer;

import javax.imageio.ImageIO;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

public class Texture {
	public final BufferedImage bi;
	public final int textureId;
	public final int height;
	public final int width;
	
	private Texture(URL path, int interpolation) throws IOException {
		bi = ImageIO.read(path);
		height = bi.getHeight();
		width = bi.getWidth();
		
		ByteBuffer buf = loadBuffer(bi, true);
		textureId = GL11.glGenTextures();
		GL11.glBindTexture(GL_TEXTURE_2D, textureId);
		GL11.glPixelStorei(GL_UNPACK_ALIGNMENT, 1);
		GL11.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL12.GL_CLAMP_TO_EDGE);
		GL11.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL12.GL_CLAMP_TO_EDGE);
		GL11.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, interpolation);
		GL11.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, interpolation);
		GL11.glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, width, height, 0, GL_RGBA, GL_UNSIGNED_BYTE, buf);
	}
	
	public void bind() {
		GL11.glBindTexture(GL_TEXTURE_2D, textureId);
	}
	
	public void unbind() {
		GL11.glBindTexture(GL_TEXTURE_2D, 0);
	}

	public void cleanup() {
		glDeleteTextures(textureId);
	}
	
	@Override
	public String toString() {
		return new StringBuilder().append("Texture[id=").append(textureId).append("] (").append(width).append("x").append(height).append(")").toString();
	}
	
	public static Texture loadTexture(URL path, int interpolation) throws IOException {
		return new Texture(path, interpolation);
	}
	
	public static Texture loadGlobalTexture(String path, int interpolation) throws IOException {
		return loadTexture(new URL(path), interpolation);
	}
	
	public static Texture loadLocalTexture(String path, int interpolation) throws IOException {
		return loadTexture(Texture.class.getResource(path), interpolation);
	}
	
	public static BufferedImage loadLocalImage(String path) {
		try {
			return ImageIO.read(Texture.class.getResourceAsStream(path));
		} catch(IOException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public static ByteBuffer loadBufferLocal(String path) {
		try {
			return loadBuffer(loadLocalImage(path));
		} catch(Exception e) {
			e.printStackTrace();
		}
		return null;
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