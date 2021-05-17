package com.hardcoded.lwjgl.gui;

/**
 * A gui component.
 * 
 * @author HardCoded
 * @since v0.2
 */
public abstract class GuiComponent {
	protected float x;
	protected float y;
	protected float width;
	protected float height;
	protected GuiFont font;
	
	protected GuiComponent() {
		
	}
	
	public float getX() {
		return x;
	}
	
	public float getY() {
		return y;
	}
	
	public void setLocation(float x, float y) {
		this.x = x;
		this.y = y;
	}
	
	public float getWidth() {
		return width;
	}
	
	public float getHeight() {
		return height;
	}
	
	public void setSize(float width, float height) {
		this.width = width;
		this.height = height;
	}
	
	public GuiFont getFont() {
		return font;
	}
	
	public void setFont(GuiFont font) {
		if(font == null) return;
		this.font = font;
	}
	
	/**
	 * Render this component
	 */
	public abstract void render();
}
