package sm.lwjgl.gui;

import org.lwjgl.opengl.GL11;

import sm.lwjgl.Camera;
import sm.lwjgl.mesh.WorldRender;

public class Gui {
	private WorldRender parent;
	private Text text;
	
	public int height;
	public int width;
	
	public Gui(WorldRender parent) {
		this.parent = parent;
		text = new Text("/Consolas.ttf");
	}
	
	public void drawBox(float x, float y, float w, float h) {
		GL11.glBegin(GL11.GL_TRIANGLE_FAN);
			GL11.glVertex2f(x    , y    );
			GL11.glVertex2f(x + w, y    );
			GL11.glVertex2f(x + w, y + h);
			GL11.glVertex2f(x    , y + h);
		GL11.glEnd();
	}
	
	
	public void render() {
		GL11.glEnable(GL11.GL_BLEND);
		GL11.glEnable(GL11.GL_ALPHA);
		GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
		
		// TODO: Gui ...
		GL11.glColor3f(1, 1, 1);
		text.drawText("Testing", 0, 0, 24);
		text.drawText("Fps: " + parent.getFps(), 0, 24, 24);
		Camera cam = parent.camera;
		text.drawText(String.format("Pos: %8.5f, %8.5f, %8.5f", cam.x, cam.y, cam.z), 0, 48, 24);
		
		GL11.glDisable(GL11.GL_BLEND);
		GL11.glDisable(GL11.GL_ALPHA);
	}
}
