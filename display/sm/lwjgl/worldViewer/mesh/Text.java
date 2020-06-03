package sm.lwjgl.worldViewer.mesh;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.RenderingHints;
import java.awt.Transparency;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import org.lwjgl.opengl.GL11;

public class Text {
	private static final String CHARACTERS =
		"abcdefghijklmnopqrstuvwxyzåäö" +
		"ABCDEFGHIJKLMNOPQRSTUVWXYZÅÄÖ" +
		"0123456789" +
		"._- *()[]{}?+/\\.,<>':";
	
	private static final int ATTLAS_HEIGHT = 1024;
	private static final int ATTLAS_WIDTH = 1024;
	private static final float ATTLAS_SIZE = 44.3f;
	private static final int ATTLAS_SPACE = (int)(0.08265 * ATTLAS_SIZE);
	private Texture texture;
	
	public Text(String name) {
		try {
			//createFontAttlas(name);
			texture = Texture.loadLocalTexture("/font.png", GL11.GL_LINEAR);
		} catch(IOException e) {
			e.printStackTrace();
		}
	}
	
	protected void createFontAttlas(String name) throws IOException {
		Font font = null;
		try {
			font = Font.createFont(Font.TRUETYPE_FONT, Text.class.getResourceAsStream(name));
			font = font.deriveFont(ATTLAS_SIZE);
		} catch(Exception e) {
			font = new Font("Arial", Font.PLAIN, 20);
			e.printStackTrace();
		}
		
		GraphicsEnvironment env = GraphicsEnvironment.getLocalGraphicsEnvironment();
		GraphicsDevice device = env.getDefaultScreenDevice();
		GraphicsConfiguration config = device.getDefaultConfiguration();
		
		BufferedImage attlas = config.createCompatibleImage(ATTLAS_WIDTH, ATTLAS_HEIGHT, Transparency.TRANSLUCENT);
		
		Graphics2D g = attlas.createGraphics();
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g.setFont(font);
		
		{
			g.setColor(Color.WHITE);
			
			int max_width = (int)(ATTLAS_WIDTH / (box.getWidth() + ATTLAS_SPACE));
			for(int i = 0; i < CHARACTERS.length(); i++) {
				char c = CHARACTERS.charAt(i);
				
				float x = (float)(box.getWidth() + ATTLAS_SPACE) * (i % max_width);
				float y = (float)(box.getHeight() + ATTLAS_SPACE) * (i / max_width) - (float)box.getY();
				g.drawString("" + c, (int)x, (int)y);
			}
		}
		
		ImageIO.write(attlas, "png", new File("res/font.png"));
	}
	
	public static final Rectangle2D box = new Rectangle2D.Float(
		0,
		-0.828125f * ATTLAS_SIZE,
		0.55f * ATTLAS_SIZE,
		1.1708984f * ATTLAS_SIZE
	);
	
	public void drawText(String chars, float x, float y, float scale) {
		int max_width = (int)(ATTLAS_WIDTH / (box.getWidth() + ATTLAS_SPACE));
		scale *= (1 / box.getHeight());
		
		GL11.glEnable(GL11.GL_TEXTURE_2D);
		texture.bind();
		GL11.glBegin(GL11.GL_TRIANGLES);
		for(int i = 0; i < chars.length(); i++) {
			int index = CHARACTERS.indexOf(chars.charAt(i));
			
			double xx = (box.getWidth() + ATTLAS_SPACE) * (index % max_width);
			double yy = (box.getHeight() + ATTLAS_SPACE) * (index / max_width);
			
			double x0 = ((int)xx) / (double)ATTLAS_WIDTH;
			double y0 = ((int)yy) / (double)ATTLAS_HEIGHT;
			double x1 = ((int)(xx + box.getWidth())) / (double)ATTLAS_WIDTH;
			double y1 = ((int)(yy + box.getHeight())) / (double)ATTLAS_HEIGHT;
			
			y0 = 1 - y0;
			y1 = 1 - y1;
			
			double vx = scale * box.getWidth() * i + x;
			double vy = y;
			double vw = scale * box.getWidth();
			double vh = scale * box.getHeight();
			
			GL11.glTexCoord2d(x0, y0);
			GL11.glVertex2d(vx     , vy     );
			GL11.glTexCoord2d(x1, y0);
			GL11.glVertex2d(vx + vw, vy     );
			GL11.glTexCoord2d(x1, y1);
			GL11.glVertex2d(vx + vw, vy + vh);
			
			
			GL11.glTexCoord2d(x0, y0);
			GL11.glVertex2d(vx     , vy     );
			GL11.glTexCoord2d(x1, y1);
			GL11.glVertex2d(vx + vw, vy + vh);
			GL11.glTexCoord2d(x0, y1);
			GL11.glVertex2d(vx     , vy + vh);
			
		}
		GL11.glEnd();
		texture.unbind();
		GL11.glDisable(GL11.GL_TEXTURE_2D);
	}
}
