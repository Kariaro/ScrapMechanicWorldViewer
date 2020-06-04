package sm.lwjgl;

import static org.lwjgl.glfw.GLFW.*;

import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.lwjgl.opengl.GL11;

import sm.lwjgl.input.Input;
import sm.lwjgl.util.MathUtils;

public class Camera {
	private final long window;
	public float x;
	public float y;
	public float z;
	
	public float rx;
	public float ry;
	public float rz;
	
	public Camera(long window) {
		this.window = window;
		//x = -1;
		//y =  4;
		//z = -5;
	}
	
	// TODO: Rework :P
	private Vector2f mouse = new Vector2f(0, 0);
	private Vector2f delta = new Vector2f(0, 0);
	private void updateMouse() {
		// TODO: Use Mouse.java ?!
		double[] x = new double[1];
		double[] y = new double[1];
		glfwGetCursorPos(window, x, y);
		
		delta.x = mouse.x - (float)x[0];
		delta.y = mouse.y - (float)y[0];
		mouse.x = (float)x[0];
		mouse.y = (float)y[0];
	}
	
	public boolean freeze = false;
	public boolean fast = false;
	public void update() {
		updateMouse();
		
		if(Input.pollKey(GLFW_KEY_C)) {
			freeze = !freeze;
		}
		
		if(!freeze) {
			rx -= delta.x / 2.0f;
			ry -= delta.y / 2.0f;
		}
		
		if(ry < -90) ry = -90;
		if(ry >  90) ry =  90;
		if(rx <   0) rx += 360;
		if(rx > 360) rx -= 360;
		
		boolean forwards = Input.keys[GLFW_KEY_W];
		boolean right = Input.keys[GLFW_KEY_A];
		boolean left = Input.keys[GLFW_KEY_D];
		boolean backwards = Input.keys[GLFW_KEY_S];
		boolean up = Input.keys[GLFW_KEY_SPACE];
		boolean down = Input.keys[GLFW_KEY_LEFT_SHIFT];
		float speed = 0.1f * (fast ? 10:1);
		if(Input.pollKey(GLFW_KEY_LEFT_CONTROL)) {
			fast = !fast;
		}
		
		int xd = 0;
		int yd = 0;
		int zd = 0;
		
		if(forwards) zd ++;
		if(backwards) zd --;
		if(right) xd --;
		if(left) xd ++;
		if(up) yd ++;
		if(down) yd --;
		
		float xx = xd * MathUtils.cosDeg(rx) + zd * MathUtils.sinDeg(rx);
		float zz = xd * MathUtils.sinDeg(rx) - zd * MathUtils.cosDeg(rx);
		float yy = yd;
		
		x += xx * speed;
		y += yy * speed;
		z += zz * speed;
		
		// x = y = z = 0;
	}
	
	public Matrix4f getViewMatrix(float fov, float width, float height) {
		Matrix4f projectionMatrix = new Matrix4f();
		projectionMatrix.setPerspective((float)Math.toRadians(fov), width / height, 0.0001f, 100);
		return projectionMatrix;
	}
	
	public Matrix4f getProjectionViewMatrix(float fov, float width, float height) {
		Matrix4f projectionMatrix = new Matrix4f();
		projectionMatrix.setPerspective((float)Math.toRadians(fov), width / height, 0.0001f, 100);
		return projectionMatrix.mul(new Matrix4f().translate(-x, -y, -z));
	}
	
	public Matrix4f getProjectionMatrix(float fov, float width, float height) {
		Matrix4f projectionMatrix = new Matrix4f();
		projectionMatrix.setPerspective((float)Math.toRadians(fov), width / height, 0.01f, 10000);
		return projectionMatrix
				.rotate(MathUtils.toRadians(ry), 1, 0, 0)
				.rotate(MathUtils.toRadians(rx), 0, 1, 0)
				.rotate(MathUtils.toRadians(rz), 0, 0, 1)
				.translate(-x, -y, -z);
	}
	
	public void setTransform() {
		GL11.glRotatef(ry, 1, 0, 0);
		GL11.glRotatef(rx, 0, 1, 0);
		GL11.glRotatef(rz, 0, 0, 1);
		GL11.glTranslatef(-x, -y, -z);
	}
}
