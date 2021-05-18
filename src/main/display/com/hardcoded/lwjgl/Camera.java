package com.hardcoded.lwjgl;

import static org.lwjgl.glfw.GLFW.*;

import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.lwjgl.opengl.GL11;

import com.hardcoded.lwjgl.input.Input;
import com.hardcoded.lwjgl.util.MathUtils;

/**
 * A simple camera implementation.
 * 
 * @author HardCoded
 * @since v0.1
 */
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
	}
	
	// TODO: Get this data from the glfw callback and not the global mouse position :P
	private Vector2f mouse = new Vector2f(0, 0);
	private Vector2f delta = new Vector2f(0, 0);
	private void updateMouse() {
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
	public int speedMod = 1;
	public void update() {
		updateMouse();
		
		if(Input.pollKey(GLFW_KEY_C)) {
			freeze = !freeze;
		}
		
		if(!freeze) {
			rx -= delta.x / 2.0f;
			ry -= delta.y / 2.0f;
		}
		
		if(ry < -180) ry = -180;
		if(ry >    0) ry = 0;
		
		if(rx <   0) rx += 360;
		if(rx > 360) rx -= 360;
		
		boolean forwards = Input.keys[GLFW_KEY_W];
		boolean right = Input.keys[GLFW_KEY_A];
		boolean left = Input.keys[GLFW_KEY_D];
		boolean backwards = Input.keys[GLFW_KEY_S];
		boolean up = Input.keys[GLFW_KEY_SPACE];
		boolean down = Input.keys[GLFW_KEY_LEFT_SHIFT];
		if(Input.pollKey(GLFW_KEY_LEFT_CONTROL)) {
			speedMod++;
			if(speedMod > 4) {
				speedMod = 1;
			}
		}
		
		float speed = 0.025f * (float)Math.pow(5, speedMod - 1);
//		if(Input.keys[GLFW_KEY_F]) {
//			speed *= 100;
//		}
		
		int xd = 0;
		int zd = 0;
		int yd = 0;
		
		if(forwards) yd --;
		if(backwards) yd ++;
		if(right) xd --;
		if(left) xd ++;
		if(up) zd ++;
		if(down) zd --;
		
		float xx = xd * MathUtils.cosDeg(-rx) + yd * MathUtils.sinDeg(-rx);
		float yy = xd * MathUtils.sinDeg(-rx) - yd * MathUtils.cosDeg(-rx);
		float zz = zd;
		
		float time_delta = 0.01f;
		if(last_time == 0) {
			last_time = System.nanoTime();
		} else {
			long now = System.nanoTime();
			time_delta = (now - last_time) / 10000000.0f;
			last_time = now;
		}
		
		speed *= time_delta;
		
		x += xx * speed;
		y += yy * speed;
		z += zz * speed;
	}
	
	private long last_time;
	
	public Vector3f getPosition() {
		return new Vector3f(x, y, z);
	}
	
	public Matrix4f getViewMatrix() {
		return new Matrix4f()
			.rotate(MathUtils.toRadians(ry), 1, 0, 0)
			.rotate(MathUtils.toRadians(rx), 0, 0, 1)
			.rotate(MathUtils.toRadians(rz), 0, 1, 0)
			.translate(-x, -y, -z);
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
			.translate(-x, -y, -z);
	}
	
	public Vector3f getViewDirection() {
		Vector3f vector = new Vector3f(0, 0, 1)
				.rotateAxis(MathUtils.toRadians(ry), 1, 0, 0)
				.rotateAxis(MathUtils.toRadians(rx), 0, 0, 1)
				.rotateAxis(MathUtils.toRadians(rz), 0, 1, 0);
		return vector;
	}
	
	public void setTransform() {
		GL11.glRotatef(rx, 0, 0, 1);
		GL11.glRotatef(ry, 1, 0, 0);
		GL11.glRotatef(rz, 0, 1, 0);
		GL11.glTranslatef(-x, -y, -z);
	}
	
	public float getYaw() {
		return rx;
	}
	
	public float getPitch() {
		return ry;
	}
}
