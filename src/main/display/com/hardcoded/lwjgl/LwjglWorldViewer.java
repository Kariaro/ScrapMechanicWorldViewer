package com.hardcoded.lwjgl;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.system.MemoryUtil.*;

import java.awt.image.BufferedImage;

import org.lwjgl.glfw.GLFWFramebufferSizeCallback;
import org.lwjgl.glfw.GLFWImage;
import org.lwjgl.glfw.GLFWVidMode;
import org.lwjgl.opengl.GL;

import com.hardcoded.lwjgl.input.Input;
import com.hardcoded.lwjgl.mesh.Texture;

public class LwjglWorldViewer implements Runnable {
	public static final BufferedImage ICON = null;
	public static final int TARGET_FPS = 120;
	
	private WorldRender render;
	private boolean running;
	private long window;
	private int fps;
	
	private Thread runningThread;
	public synchronized void start() {
		if(running || (runningThread != null && runningThread.isAlive())) return;
		running = true;
		runningThread = new Thread(this, "Main Thread");
		runningThread.start();
	}
	
	public synchronized void stop() {
		running = false;
		try {
			runningThread.join();
		} catch(InterruptedException e) {
			e.printStackTrace();
		}
		
		runningThread = null;
	}
	
	private void init() throws Exception {
		if(!glfwInit()) {
			return;
		}

		int height = (int)(540 * 1.5);
		int width = (int)(960);
		
		glfwWindowHint(GLFW_SAMPLES, 4);
		glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE);
		glfwWindowHint(GLFW_RESIZABLE, GLFW_TRUE);
		window = glfwCreateWindow(width, height, "ScrapMechanic - viewer", NULL, NULL);
		if(window == NULL) {
			throw new NullPointerException("Failed to initialize the window");
		}
		
		GLFWVidMode vidmode = glfwGetVideoMode(glfwGetPrimaryMonitor());
		glfwSetWindowPos(window, (vidmode.width() - width) / 2, (vidmode.height() - height) / 2);
		
		glfwSetKeyCallback(window, new Input());
		//glfwSetCursorPosCallback(window, new Mouse(0));
		//glfwSetMouseButtonCallback(window, new Mouse(1));
		glfwSetInputMode(window, GLFW_CURSOR, GLFW_CURSOR_NORMAL);
		glfwSetFramebufferSizeCallback(window, new GLFWFramebufferSizeCallback() {
			public void invoke(long window, int width, int height) {
				render.setViewport(width, height);
			}
		});
		
		glfwMakeContextCurrent(window);
		if(ICON != null) {
			GLFWImage image = GLFWImage.malloc();
			GLFWImage.Buffer buffer = GLFWImage.malloc(1);
			image.set(ICON.getWidth(), ICON.getHeight(), Texture.loadBuffer(ICON));
			buffer.put(0, image);
			glfwSetWindowIcon(window, buffer);
		}
		
		GL.createCapabilities();
		
		render = new WorldRender(this, window, width, height);
		glfwShowWindow(window);
	}
	
	public int getFps() {
		return fps;
	}
	
	public void run() {
		try {
			init();
		} catch(Exception e) {
			e.printStackTrace();
			System.exit(0);
		}
		
		double SLEEP_TIME = 1000.0 / (double)TARGET_FPS;
		
		int frames = 0;
		long last = System.currentTimeMillis();
		double next = System.currentTimeMillis() + SLEEP_TIME;
		while(running) {
			try {
				long aaaa = System.currentTimeMillis();
				if(aaaa < next) {
					Thread.sleep((long)(next - aaaa));
				}
				next += SLEEP_TIME;
				if(aaaa > next + SLEEP_TIME) {
					next += (long)((aaaa - next) / SLEEP_TIME) * SLEEP_TIME;
					// Target fps not reached!
					// Fps is lower than TARGET_FPS
				}
			} catch(InterruptedException e) {
				e.printStackTrace();
			}
			
			try {
				render.render();
				render.update();
				frames++;
			} catch(Exception e) {
				e.printStackTrace();
			}
			
			long now = System.currentTimeMillis();
			if(now - last > 1000) {
				fps = frames;
				
				frames = 0;
				last += 1000;
			}
			
			if(glfwWindowShouldClose(window)) {
				running = false;
			}
		}
		
		glfwDestroyWindow(window);
		glfwTerminate();
	}
}
