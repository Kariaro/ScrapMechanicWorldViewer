package com.hardcoded.lwjgl;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.system.MemoryUtil.*;

import java.awt.image.BufferedImage;
import java.util.concurrent.ConcurrentLinkedDeque;

import org.lwjgl.glfw.*;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.WGL;
import org.lwjgl.system.windows.User32;

import com.hardcoded.lwjgl.async.LwjglAsyncThread;
import com.hardcoded.lwjgl.data.Texture;
import com.hardcoded.lwjgl.input.Input;

/**
 * This is the main thread of the lwjgl application.
 * 
 * @author HardCoded
 * @since v0.1
 */
public class LwjglWorldViewer implements Runnable {
	private static LwjglWorldViewer INSTANCE;
	
	public static final BufferedImage ICON = null;
	public static final int TARGET_FPS = 240;
	
	protected final ConcurrentLinkedDeque<Runnable> tasks;
	private WorldRender render;
	private boolean running;
	private long window;
	private int fps;
	
	private Thread runningThread;
	private Thread asyncThread;
	public LwjglWorldViewer() {
		INSTANCE = this;
		
		tasks = new ConcurrentLinkedDeque<>();
	}
	
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
			Thread.currentThread().interrupt();
		}
		
		runningThread = null;
	}
	
	public int getFps() {
		return fps;
	}
	
	private boolean init() {
		if(!glfwInit()) {
			return false;
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
		GL.createCapabilitiesWGL();
		
		long hwnd = GLFWNativeWin32.glfwGetWin32Window(window);
		long dc = User32.GetDC(hwnd);
		
		long context_1 = WGL.wglCreateContext(dc);
		long context_2 = WGL.wglCreateContext(dc);
		if(!WGL.wglShareLists(context_1, context_2)) {
			System.err.println("Failed to create shared list!");
			WGL.wglDeleteContext(context_2);
		}
		
		asyncThread = new Thread(new LwjglAsyncThread(dc, context_2), "Async Thread");
		asyncThread.setDaemon(true);
		asyncThread.start();
		
		WGL.wglMakeCurrent(dc, context_1);
		render = new WorldRender(this, window, width, height);
		glfwShowWindow(window);
		
		return true;
	}
	
	@Override
	public void run() {
		if(!init()) {
			throw new RuntimeException("Failed to initialize the LWJGL window");
		}
		
		double SLEEP_TIME = 1000.0 / (double)TARGET_FPS;
		
		int frames = 0;
		long last = System.currentTimeMillis();
		double next = System.currentTimeMillis() + SLEEP_TIME;
		try {
			while(running) {
				// Run tasks
				while(!tasks.isEmpty()) {
					tasks.poll().run();
				}
				
				{
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
		} catch(InterruptedException e) {
			e.printStackTrace();
		}
		
		glfwDestroyWindow(window);
		glfwTerminate();
	}
	
	/**
	 * Returns {@code true} if the current thread is running on the main thread.
	 * @return {@code true} if the current thread is running on the main thread
	 */
	public static boolean isCurrentThread() {
		return Thread.currentThread() == INSTANCE.runningThread;
	}
	
	/**
	 * Run a task on the main thread.
	 * @param runnable a task
	 */
	public static void runLater(Runnable runnable) {
		INSTANCE.tasks.add(runnable);
	}
}
