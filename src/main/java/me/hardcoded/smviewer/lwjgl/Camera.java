package me.hardcoded.smviewer.lwjgl;

import static org.lwjgl.glfw.GLFW.*;

import me.hardcoded.smviewer.lwjgl.input.Input;
import me.hardcoded.smviewer.lwjgl.util.Average;
import me.hardcoded.smviewer.lwjgl.util.MathUtils;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.opengl.GL11;

/**
 * A simple camera implementation.
 * 
 * @author HardCoded
 * @since v0.1
 */
public class Camera {
	private final long window;
	public double x;
	public double y;
	public double z;
	
	private double x_vel;
	private double y_vel;
	private double z_vel;
	
	public double rx;
	public double ry;
	public double rz;
	
	public Camera(long window) {
		this.window = window;
	}
	
	private boolean isMouseCaptured = false;
	private double mouseScroll = 1;
	
	public Average average = new Average(10);
	
	public boolean isMouseCaptured() {
		return isMouseCaptured;
	}
	
	public double getSpeedModifier() {
		return Math.pow(4, mouseScroll);
	}
	
	public void update() {
		if (Input.pollKey(GLFW_KEY_LEFT_ALT)) {
			isMouseCaptured = !isMouseCaptured;
			glfwSetInputMode(window, GLFW_CURSOR, isMouseCaptured ? GLFW_CURSOR_DISABLED : GLFW_CURSOR_NORMAL);
		}
		
		if (isMouseCaptured) {
			rx += Input.getMouseDeltaX() / 4.0f;
			ry += Input.getMouseDeltaY() / 4.0f;
		}
		
		if (ry < -180) ry = -180;
		if (ry >    0) ry = 0;
		
		if (rx <   0) rx += 360;
		if (rx > 360) rx -= 360;
		
		boolean forwards = Input.keys[GLFW_KEY_W];
		boolean right = Input.keys[GLFW_KEY_A];
		boolean left = Input.keys[GLFW_KEY_D];
		boolean backwards = Input.keys[GLFW_KEY_S];
		boolean up = Input.keys[GLFW_KEY_SPACE];
		boolean down = Input.keys[GLFW_KEY_LEFT_SHIFT];
		
		{
			double newScroll = (int)(Input.getScrollDeltaY() + mouseScroll * 10) / 10.0;
			
			if (newScroll < -1) {
				newScroll = -1;
			}
			if (newScroll > 5) {
				newScroll = 5;
			}
			
			mouseScroll = newScroll;
		}
		
		int xd = 0;
		int zd = 0;
		int yd = 0;
		
		if (forwards) yd --;
		if (backwards) yd ++;
		if (right) xd --;
		if (left) xd ++;
		if (up) zd ++;
		if (down) zd --;
		
		double xx = xd * MathUtils.cosDeg(-rx) + yd * MathUtils.sinDeg(-rx);
		double yy = xd * MathUtils.sinDeg(-rx) - yd * MathUtils.cosDeg(-rx);
		double zz = zd;
		
//		double den = 1.0 / Math.sqrt(xx * xx + yy * yy);
//		if (Double.isFinite(den)) {
//			xx *= den;
//			yy *= den;
//		}
		
		double time_delta = LwjglWindowSetup.getDeltaTime();
		double speed = 10 * getSpeedModifier();
		
		double lx = x;
		double ly = y;
		double lz = z;
		
		x_vel += xx * speed * time_delta;
		y_vel += yy * speed * time_delta;
		z_vel += zz * speed * time_delta;
		
		double dampening = Math.pow(0.001, time_delta);
		x_vel *= dampening;
		y_vel *= dampening;
		z_vel *= dampening;
		
		x += x_vel * time_delta;
		y += y_vel * time_delta;
		z += z_vel * time_delta;
		
		double dx = x - lx;
		double dy = y - ly;
		double dz = z - lz;
		average.add(Math.sqrt(dx * dx + dy * dy + dz * dz));
	}
	
	public Vector3f getPosition() {
		return new Vector3f((float)x, (float)y, (float)z);
	}
	
	public Matrix4f getViewMatrix() {
		return new Matrix4f()
			.rotate(MathUtils.toRadians(ry), 1, 0, 0)
			.rotate(MathUtils.toRadians(rx), 0, 0, 1)
			.rotate(MathUtils.toRadians(rz), 0, 1, 0)
			.translate((float)-x, (float)-y, (float)-z);
	}
	
//	public Matrix4f getProjectionViewMatrix(float fov, float width, float height) {
//		Matrix4f projectionMatrix = new Matrix4f();
//		projectionMatrix.setPerspective((float)Math.toRadians(fov), width / height, 0.01f, 100000);
//		return projectionMatrix.mul(new Matrix4f().translate(-x, -y, -z));
//	}
	
	public Matrix4f getProjectionMatrix(float fov, float width, float height) {
		Matrix4f projectionMatrix = new Matrix4f();
		projectionMatrix.setPerspective((float)Math.toRadians(fov), width / height, 0.5f, 10000000);
		return projectionMatrix
			.rotate(MathUtils.toRadians(ry), 1, 0, 0)
			.rotate(MathUtils.toRadians(rx), 0, 0, 1)
			.rotate(MathUtils.toRadians(rz), 0, 1, 0)
			.translate((float)-x, (float)-y, (float)-z);
	}
	
	public Vector3f getViewDirection() {
		return new Vector3f(0, 0, 1)
			.rotateAxis(MathUtils.toRadians(ry), 1, 0, 0)
			.rotateAxis(MathUtils.toRadians(rx), 0, 0, 1)
			.rotateAxis(MathUtils.toRadians(rz), 0, 1, 0);
	}
	
	public void setTransform() {
		GL11.glRotated(rx, 0, 0, 1);
		GL11.glRotated(ry, 1, 0, 0);
		GL11.glRotated(rz, 0, 1, 0);
		GL11.glTranslated(-x, -y, -z);
	}
	
	public float getYaw() {
		return (float)rx;
	}
	
	public float getPitch() {
		return (float)ry;
	}
}
