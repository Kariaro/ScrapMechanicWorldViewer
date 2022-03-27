package me.hardcoded.smviewer.lwjgl.gui;

import java.awt.geom.Rectangle2D;

import org.lwjgl.opengl.GL11;

/**
 * A gui text component. This component is used to draw text
 * on the screen.
 * 
 * @author HardCoded
 * @since v0.2
 * 
 * TODO: Use a shader to render the text
 */
public class GuiText extends GuiComponent {
	protected String text = "";
	protected GuiFont font;
	protected float fontSize = 24.0f;
	
	public GuiText() {
		
	}
	
	public GuiText(String text) {
		this.text = (text == null) ? "":text;
	}
	
	public void setText(String text) {
		this.text = (text == null) ? "":text;
	}
	
	public void setFont(GuiFont font) {
		if (font == null) return;
		this.font = font;
	}
	
	public void setFontSize(float size) {
		this.fontSize = size;
	}
	
	@Override
	public void render() {
		if (font == null) return;

		GL11.glEnable(GL11.GL_TEXTURE_2D);
		font.bind();
		
		{
			String CHARACTERS = GuiFont.CHARACTERS;
			float ATLAS_WIDTH = GuiFont.ATLAS_WIDTH;
			float ATLAS_HEIGHT = GuiFont.ATLAS_HEIGHT;
			float ATLAS_SPACE = GuiFont.ATLAS_SPACE;
			Rectangle2D box = GuiFont.box;
			String chars = text;
			
			int max_width = (int)(ATLAS_WIDTH / (box.getWidth() + ATLAS_SPACE));
			double scale = fontSize * (1 / box.getHeight());
			
			GL11.glBegin(GL11.GL_TRIANGLES);
			for (int i = 0; i < chars.length(); i++) {
				int index = CHARACTERS.indexOf(chars.charAt(i));
				
				double xx = (box.getWidth() + ATLAS_SPACE) * (index % max_width);
				double yy = (box.getHeight() + ATLAS_SPACE) * (index / max_width);
				
				double x0 = ((int)xx) / (double)ATLAS_WIDTH;
				double y0 = ((int)yy) / (double)ATLAS_HEIGHT;
				double x1 = ((int)(xx + box.getWidth())) / (double)ATLAS_WIDTH;
				double y1 = ((int)(yy + box.getHeight())) / (double)ATLAS_HEIGHT;
				
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
		}
		
		font.unbind();
		GL11.glDisable(GL11.GL_TEXTURE_2D);
	}
	
	public void drawText(String chars, float x, float y, float scale) {
		
	}
}
