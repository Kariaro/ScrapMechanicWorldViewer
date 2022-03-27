package me.hardcoded.smviewer.lwjgl;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.system.MemoryUtil.*;

import java.awt.image.BufferedImage;
import java.util.concurrent.ConcurrentLinkedDeque;

import me.hardcoded.smviewer.lwjgl.async.LwjglAsyncThread;
import me.hardcoded.smviewer.lwjgl.data.ImageLoader;
import me.hardcoded.smviewer.lwjgl.input.Input;
import me.hardcoded.smviewer.lwjgl.util.LoadingException;
import org.lwjgl.glfw.*;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.WGL;
import org.lwjgl.system.windows.User32;

/**
 * This is the main thread of the lwjgl application.
 * 
 * @author HardCoded
 * @since v0.1
 */
public class LwjglWindowSetup implements Runnable {
	private static LwjglWindowSetup INSTANCE;
	private static double deltaTime;
	
	public static int TARGET_FPS = 60;
	
	protected final ConcurrentLinkedDeque<Runnable> tasks;
	private WorldRender render;
	private boolean running;
	private long window;
	private int fps;
	
	private Thread runningThread;
	private Thread asyncThread;
	
	public LwjglWindowSetup() {
		INSTANCE = this;
		
		tasks = new ConcurrentLinkedDeque<>();
	}
	
	public synchronized void start() {
		if (running || (runningThread != null && runningThread.isAlive())) {
			return;
		}
		
		running = true;
		runningThread = new Thread(this, "Main Thread");
		runningThread.start();
	}
	
	public synchronized void stop() {
		running = false;
		try {
			runningThread.join();
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		}
		
		runningThread = null;
	}
	
	public int getFps() {
		return fps;
	}
	
	private GLFWImage.Buffer createIconBuffer() {
		BufferedImage[] icons = {
			ImageLoader.loadResourceImage("/icons/icon_16.png"),
			ImageLoader.loadResourceImage("/icons/icon_32.png"),
			ImageLoader.loadResourceImage("/icons/icon_64.png"),
			ImageLoader.loadResourceImage("/icons/icon_256.png")
		};
		
		GLFWImage.Buffer buffer = GLFWImage.malloc(icons.length);
		for (int i = 0; i < icons.length; i++) {
			BufferedImage bi = icons[i];
			GLFWImage image = GLFWImage.malloc();
			image.set(bi.getWidth(), bi.getHeight(), ImageLoader.createBuffer(bi));
			buffer.put(i, image);
		}
		
		return buffer;
	}
	
	private boolean init() {
		if (!glfwInit()) {
			return false;
		}

		int height = (int)(540 * 1.5);
		int width = (int)(960);
		
		glfwWindowHint(GLFW_SAMPLES, 4);
		glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE);
		glfwWindowHint(GLFW_RESIZABLE, GLFW_TRUE);
		window = glfwCreateWindow(width, height, "ScrapMechanic - viewer", NULL, NULL);
		if (window == NULL) {
			throw new LoadingException("Failed to initialize the window: window == NULL");
		}
		
		GLFWVidMode videoMode = glfwGetVideoMode(glfwGetPrimaryMonitor());
		glfwSetWindowPos(window, (videoMode.width() - width) / 2, (videoMode.height() - height) / 2);
		
		glfwSetKeyCallback(window, new Input.Keyboard());
		glfwSetScrollCallback(window, new Input.MouseScroll());
		glfwSetCursorPosCallback(window, new Input.MousePosition());
		glfwSetInputMode(window, GLFW_CURSOR, GLFW_CURSOR_NORMAL);
		glfwSetFramebufferSizeCallback(window, new GLFWFramebufferSizeCallback() {
			public void invoke(long window, int width, int height) {
				render.setViewport(width, height);
			}
		});
		
		glfwMakeContextCurrent(window);
		glfwSetWindowIcon(window, createIconBuffer());
		
		GL.createCapabilities();
		GL.createCapabilitiesWGL();
		
		long hwnd = GLFWNativeWin32.glfwGetWin32Window(window);
		long dc = User32.GetDC(hwnd);
		
		long context_1 = WGL.wglCreateContext(dc);
		long context_2 = WGL.wglCreateContext(dc);
		
		boolean success = true;
		if (!WGL.wglShareLists(context_1, context_2)) {
			System.err.println("Failed to create shared list!");
			WGL.wglDeleteContext(context_2);
			success = false;
		}
		
		asyncThread = new Thread(new LwjglAsyncThread(dc, context_2, success), "Async Thread");
		asyncThread.setDaemon(true);
		asyncThread.start();
		
		WGL.wglMakeCurrent(dc, context_1);
		render = new WorldRender(this, window, width, height);
		glfwShowWindow(window);
		
		return true;
	}
	
	@Override
	public void run() {
		if (!init()) {
			throw new RuntimeException("Failed to initialize the LWJGL window");
		}
		
		// TODO: Upgrade the render loop to make it more accurate.
		//       The current implementation of sleep will sleep to much in some cases.
		
		int frames = 0;
		long last = System.currentTimeMillis();
		double next = System.currentTimeMillis();
		
		long lastFrame = System.nanoTime();
		while (running) {
			double SLEEP_TIME = 1000.0 / (double)TARGET_FPS;
			
			if (Input.pollKey(GLFW_KEY_U)) {
				TARGET_FPS = (TARGET_FPS == 60) ? 15 : 60;
			}
			// Run tasks
			while (!tasks.isEmpty()) {
				tasks.poll().run();
			}
			
			while (System.currentTimeMillis() < next) {
				try {
					Thread.sleep(1);
				} catch (InterruptedException e) {
					e.printStackTrace();
					
					// Stop the running loop
					running = false;
					break;
				}
			}
			
			{
				long currentTime = System.currentTimeMillis();
				next += SLEEP_TIME;
				if (currentTime > next + SLEEP_TIME) {
					next += (long)((currentTime - next) / SLEEP_TIME) * SLEEP_TIME;
					
					// Target fps not reached!
					// Fps is lower than TARGET_FPS
//					System.out.println("Target fps not reached");
				}
			}
			
			try {
				long currentFrame = System.nanoTime();
				deltaTime = (currentFrame - lastFrame) / 1000000000.0;
				lastFrame = currentFrame;
				
				render.render();
				render.update();
				frames++;
			} catch (Exception e) {
				e.printStackTrace();
			}
			
			long now = System.currentTimeMillis();
			if (now - last > 1000) {
				fps = frames;
				frames = 0;
				last += 1000;
				System.gc();
			}
			
			if (glfwWindowShouldClose(window)) {
				running = false;
			}
		}
		
		glfwDestroyWindow(window);
		glfwTerminate();
	}
	
	/**
	 * Returns the current delta time since the last frame
	 */
	public static double getDeltaTime() {
		return deltaTime;
	}
	
	/**
	 * Returns {@code true} if the current thread is running on the main thread
	 */
	public static boolean isCurrentThread() {
		return Thread.currentThread() == INSTANCE.runningThread;
	}
	
	/**
	 * Run a task on the main thread
	 * @param runnable a task
	 */
	public static void runLater(Runnable runnable) {
		INSTANCE.tasks.add(runnable);
	}
}
