package me.hardcoded.smviewer.lwjgl.gui;

import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.lwjgl.opengl.GL11;

import me.hardcoded.smreader.logger.Log;
import me.hardcoded.smviewer.lwjgl.data.Texture;

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
		"abcdefghijklmnopqrstuvwxyz" +
		"ABCDEFGHIJKLMNOPQRSTUVWXYZ" +
		"0123456789" +
		"._- *()[]{}?+/\\.,<>':";
	
	protected static final int ATLAS_HEIGHT = 1024;
	protected static final int ATLAS_WIDTH = 1024;
	protected static final float ATLAS_SIZE = 44.3f;
	protected static final int ATLAS_SPACE = (int)(0.08265 * ATLAS_SIZE);
	protected static Texture texture;
	protected static final Rectangle2D box = new Rectangle2D.Float(
		0,
		-0.828125f * ATLAS_SIZE,
		0.55f * ATLAS_SIZE,
		1.1708984f * ATLAS_SIZE
	);
	
	protected final Map<Character, Glyph> glyphs;
	
	private GuiFont(String path) {
		glyphs = new HashMap<>();
		
		try {
			BufferedImage bi = createAtlasFromLocalResource(path);
			texture = Texture.loadBufferedImageTexture(bi, GL11.GL_LINEAR);
		} catch (Exception e) {
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
		
		BufferedImage atlas = config.createCompatibleImage(ATLAS_WIDTH, ATLAS_HEIGHT, Transparency.TRANSLUCENT);
		
		Graphics2D g = atlas.createGraphics();
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
		g.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
		font = font.deriveFont(48.0f);
		g.setFont(font);
		
		{
			g.setColor(Color.WHITE);
			
			int max_width = (int)(ATLAS_WIDTH / (box.getWidth() + ATLAS_SPACE));
			for (int i = 0; i < CHARACTERS.length(); i++) {
				char c = CHARACTERS.charAt(i);
				
				float x = (float)(box.getWidth() + ATLAS_SPACE) * (i % max_width);
				float y = (float)(box.getHeight() + ATLAS_SPACE) * (i / max_width) - (float)box.getY();
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
	
//	protected void createFontAtlasFromResource(String name) throws IOException {
//		Font font = null;
//		try {
//			font = Font.createFont(Font.TRUETYPE_FONT, GuiFont.class.getResourceAsStream(name));
//			font = font.deriveFont(ATLAS_SIZE);
//		} catch (Exception e) {
//			font = new Font("Arial", Font.PLAIN, 20);
//			e.printStackTrace();
//		}
//		
//		GraphicsEnvironment env = GraphicsEnvironment.getLocalGraphicsEnvironment();
//		GraphicsDevice device = env.getDefaultScreenDevice();
//		GraphicsConfiguration config = device.getDefaultConfiguration();
//		
//		BufferedImage atlas = config.createCompatibleImage(ATLAS_WIDTH, ATLAS_HEIGHT, Transparency.TRANSLUCENT);
//		
//		Graphics2D g = atlas.createGraphics();
//		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
//		g.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
//		g.setFont(font);
//		
//		{
//			g.setColor(Color.WHITE);
//			
//			int max_width = (int)(ATLAS_WIDTH / (box.getWidth() + ATLAS_SPACE));
//			for (int i = 0; i < CHARACTERS.length(); i++) {
//				char c = CHARACTERS.charAt(i);
//				
//				float x = (float)(box.getWidth() + ATLAS_SPACE) * (i % max_width);
//				float y = (float)(box.getHeight() + ATLAS_SPACE) * (i / max_width) - (float)box.getY();
//				g.drawString(String.valueOf(c), (int)x, (int)y);
//			}
//		}
//		
//		ImageIO.write(atlas, "png", new File("res/font.png"));
//	}
	
	protected static GuiFont createFromFile(String path) {
		return new GuiFont(path);
	}
}
