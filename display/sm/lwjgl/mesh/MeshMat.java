package sm.lwjgl.mesh;

import org.lwjgl.opengl.GL11;

import sm.lwjgl.shader.PartShader;

public class MeshMat {
	public boolean flat;
	public boolean flip;
	public boolean alpha;
	
	public void bind(PartShader shader) {
		if(flip) GL11.glDisable(GL11.GL_CULL_FACE);
		if(alpha) {
			//GL11.glDisable(GL11.GL_ALPHA_TEST);
			//GL11.glEnable(GL11.GL_BLEND);
			//GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
			shader.setUniform("hasAlpha", true);
		}
	}
	
	public void unbind(PartShader shader) {
		if(flip) GL11.glEnable(GL11.GL_CULL_FACE);
		if(alpha) {
			//GL11.glDisable(GL11.GL_BLEND);
			shader.setUniform("hasAlpha", false);
		}
	}
}
