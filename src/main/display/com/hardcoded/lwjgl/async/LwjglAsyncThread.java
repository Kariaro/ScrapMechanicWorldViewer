package com.hardcoded.lwjgl.async;

import java.util.concurrent.ConcurrentLinkedDeque;

import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.WGL;

import com.hardcoded.lwjgl.LwjglWorldViewer;

/**
 * This class is an asynchronious running GL context.
 * This class is used to multithread the object loading.
 * 
 * @author HardCoded
 * @since v0.2
 */
public class LwjglAsyncThread implements Runnable {
	private static LwjglAsyncThread INSTANCE;
	
	protected final ConcurrentLinkedDeque<Runnable> tasks;
	protected Thread thread;
	
	private final boolean success;
	private final long context;
	private final long dc;
	
	public LwjglAsyncThread(long dc, long context, boolean success) {
		if(INSTANCE != null) throw new RuntimeException("LwjglAsyncThread was already instantiated");
		INSTANCE = this;
		
		this.tasks = new ConcurrentLinkedDeque<>();
		this.dc = dc;
		this.context = context;
		this.success = success;
	}
	
	@Override
	public void run() {
		thread = Thread.currentThread();
		if(!success) return;
		
		// Add context to this thread
		WGL.wglMakeCurrent(dc, context);
		GL.createCapabilities();
		
		while(true) {
			try {
				Thread.sleep(10);
			} catch(InterruptedException e) {
				break;
			}
			
			while(!tasks.isEmpty()) {
				Runnable task = tasks.poll();
				task.run();
			}
		}
	}
	
	/**
	 * Returns {@code true} if the current thread is running on the async thread.
	 * @return {@code true} if the current thread is running on the async thread
	 */
	public static boolean isCurrentThread() {
		return Thread.currentThread() == INSTANCE.thread;
	}
	
	/**
	 * Add a new task to the async thread.
	 * @param runnable a task
	 */
	public static void runAsync(Runnable runnable) {
		// If we failed to share the lists we load everything on the main thread
		if(!INSTANCE.success) {
			LwjglWorldViewer.runLater(runnable);
			return;
		}
		
		INSTANCE.tasks.add(runnable);
	}
}
