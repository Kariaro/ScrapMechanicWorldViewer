package com.hardcoded.lwjgl.gui;

import java.util.ArrayList;
import java.util.List;

import org.lwjgl.opengl.GL11;

/**
 * A gui render.
 * 
 * @author HardCoded
 * @since v0.1
 */
public class GuiRender {
	protected List<GuiComponent> components;
	private GuiFont defaultFont;
	
	protected GuiRender() {
		this.defaultFont = GuiFont.createFromFile("/Consolas.ttf");
		this.components = new ArrayList<>();
	}
	
	public void drawBox(float x, float y, float w, float h) {
		GL11.glBegin(GL11.GL_TRIANGLE_FAN);
			GL11.glVertex2f(x    , y    );
			GL11.glVertex2f(x + w, y    );
			GL11.glVertex2f(x + w, y + h);
			GL11.glVertex2f(x    , y + h);
		GL11.glEnd();
	}
	
	public void add(GuiComponent comp) {
		if(comp == null || components.contains(comp)) return;
		if(comp.getFont() == null) {
			comp.setFont(defaultFont);
		}
		
		components.add(comp);
	}
	
	public void render() {
		GL11.glEnable(GL11.GL_BLEND);
		GL11.glEnable(GL11.GL_ALPHA);
		GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
		
		for(GuiComponent comp : components) {
			comp.render();
		}
		
		GL11.glDisable(GL11.GL_BLEND);
		GL11.glDisable(GL11.GL_ALPHA);
	}
}
