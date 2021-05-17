package com.hardcoded.lwjgl.gui;

import org.lwjgl.opengl.GL11;

import com.hardcoded.lwjgl.Camera;
import com.hardcoded.lwjgl.WorldRender;

/**
 * The gui manager for this project.
 * 
 * @author HardCoded
 * @since v0.2
 */
public class Gui {
	protected final WorldRender worldRender;
	protected final GuiRender guiRender;
	
	protected GuiText label_world;
	protected GuiText label_fps;
	protected GuiText label_pos;
	
	public int height;
	public int width;
	
	public Gui(WorldRender render) {
		this.worldRender = render;
		this.guiRender = new GuiRender();
		
		label_world = new GuiText();
		label_world.setLocation(0, 0);
		label_fps = new GuiText();
		label_fps.setLocation(0, 24);
		label_pos = new GuiText();
		label_pos.setLocation(0, 48);
		
		guiRender.add(label_world);
		guiRender.add(label_fps);
		guiRender.add(label_pos);
	}
	
	public void render() {
		label_world.setText(String.format("World: '%s'", WorldRender.fileName));
		label_fps.setText(String.format("Fps: '%d'", worldRender.getFps()));
		Camera cam = worldRender.camera;
		label_pos.setText(String.format("Pos: %8.5f, %8.5f, %8.5f", cam.x, cam.y, cam.z));
		
		GL11.glColor3f(1, 1, 1);
		guiRender.render();
		
//		defaultFont.drawText("World: '" + WorldRender.fileName + "'", 0, 0, 24);
//		defaultFont.drawText("Fps: " + parent.getFps(), 0, 24, 24);
//		defaultFont.drawText(String.format("Pos: %8.5f, %8.5f, %8.5f", cam.x, cam.y, cam.z), 0, 48, 24);
	}
}
