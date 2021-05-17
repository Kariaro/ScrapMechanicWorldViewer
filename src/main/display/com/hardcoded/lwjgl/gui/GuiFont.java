package com.hardcoded.lwjgl.gui;

import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.lwjgl.opengl.GL11;

import com.hardcoded.logger.Log;
import com.hardcoded.lwjgl.data.Texture;

/**
 * A font handler for lwjgl.
 * 
 * @author HardCoded
 * @since v0.1
 * 
 * TODO: The text should be rendered in a shader and not with the gl functions!
 */
public class GuiFont {
	private static final Log LOGGER = Log.getLogger();
	
	protected static final String CHARACTERS =
		"abcdefghijklmnopqrstuvwxyzåäö" +
		"ABCDEFGHIJKLMNOPQRSTUVWXYZÅÄÖ" +
		"0123456789" +
		"._- *()[]{}?+/\\.,<>':";
	
	protected static final int ATTLAS_HEIGHT = 1024;
	protected static final int ATTLAS_WIDTH = 1024;
	protected static final float ATTLAS_SIZE = 44.3f;
	protected static final int ATTLAS_SPACE = (int)(0.08265 * ATTLAS_SIZE);
	protected static Texture texture;
	protected static final Rectangle2D box = new Rectangle2D.Float(
		0,
		-0.828125f * ATTLAS_SIZE,
		0.55f * ATTLAS_SIZE,
		1.1708984f * ATTLAS_SIZE
	);
	
	protected final Map<Character, Glyph> glyphs;
	
	private GuiFont(String path) {
		glyphs = new HashMap<>();
		
		try {
			BufferedImage bi = createAtlasFromLocalResource(path);
			texture = Texture.loadBufferedImageTexture(bi, GL11.GL_LINEAR);
		} catch(Exception e) {
			LOGGER.throwing(e);
			throw new NullPointerException("Failed to load any font");
		}
	}
	
	protected void bind() {
		texture.bind();
	}
	
	protected void unbind() {
		texture.unbind();
	}
	
	protected BufferedImage createAtlasFromLocalResource(String path) throws IOException, FontFormatException {
		return createAtlas(Font.createFont(Font.TRUETYPE_FONT, GuiFont.class.getResourceAsStream(path)));
	}
	
	protected BufferedImage createAtlas(Font font) {
		GraphicsEnvironment env = GraphicsEnvironment.getLocalGraphicsEnvironment();
		GraphicsDevice device = env.getDefaultScreenDevice();
		GraphicsConfiguration config = device.getDefaultConfiguration();
		
		BufferedImage atlas = config.createCompatibleImage(ATTLAS_WIDTH, ATTLAS_HEIGHT, Transparency.TRANSLUCENT);
		
		Graphics2D g = atlas.createGraphics();
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
		g.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
		font = font.deriveFont(48.0f);
		g.setFont(font);
		
		{
			g.setColor(Color.WHITE);
			
			int max_width = (int)(ATTLAS_WIDTH / (box.getWidth() + ATTLAS_SPACE));
			for(int i = 0; i < CHARACTERS.length(); i++) {
				char c = CHARACTERS.charAt(i);
				
				float x = (float)(box.getWidth() + ATTLAS_SPACE) * (i % max_width);
				float y = (float)(box.getHeight() + ATTLAS_SPACE) * (i / max_width) - (float)box.getY();
				g.drawString(String.valueOf(c), (int)x, (int)y);
			}
		}
		
		g.dispose();
		
		return atlas;
	}
	
	protected static class Glyph {
		public final int width;
		public final int height;
		public final int x;
		public final int y;
		
		public Glyph(int x, int y, int width, int height) {
			this.x = x;
			this.y = y;
			this.width = width;
			this.height = height;
		}
	}
	
//	protected void createFontAttlasFromResource(String name) throws IOException {
//		Font font = null;
//		try {
//			font = Font.createFont(Font.TRUETYPE_FONT, GuiFont.class.getResourceAsStream(name));
//			font = font.deriveFont(ATTLAS_SIZE);
//		} catch(Exception e) {
//			font = new Font("Arial", Font.PLAIN, 20);
//			e.printStackTrace();
//		}
//		
//		GraphicsEnvironment env = GraphicsEnvironment.getLocalGraphicsEnvironment();
//		GraphicsDevice device = env.getDefaultScreenDevice();
//		GraphicsConfiguration config = device.getDefaultConfiguration();
//		
//		BufferedImage attlas = config.createCompatibleImage(ATTLAS_WIDTH, ATTLAS_HEIGHT, Transparency.TRANSLUCENT);
//		
//		Graphics2D g = attlas.createGraphics();
//		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
//		g.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
//		g.setFont(font);
//		
//		{
//			g.setColor(Color.WHITE);
//			
//			int max_width = (int)(ATTLAS_WIDTH / (box.getWidth() + ATTLAS_SPACE));
//			for(int i = 0; i < CHARACTERS.length(); i++) {
//				char c = CHARACTERS.charAt(i);
//				
//				float x = (float)(box.getWidth() + ATTLAS_SPACE) * (i % max_width);
//				float y = (float)(box.getHeight() + ATTLAS_SPACE) * (i / max_width) - (float)box.getY();
//				g.drawString(String.valueOf(c), (int)x, (int)y);
//			}
//		}
//		
//		ImageIO.write(attlas, "png", new File("res/font.png"));
//	}
	
	protected static GuiFont createFromFile(String path) {
		return new GuiFont(path);
	}
}
