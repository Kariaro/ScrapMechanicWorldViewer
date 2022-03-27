package me.hardcoded.smviewer.lwjgl.input;

import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWCursorPosCallback;
import org.lwjgl.glfw.GLFWKeyCallback;
import org.lwjgl.glfw.GLFWScrollCallback;

/**
 * @author HardCoded
 * @since v0.1
 */
public class Input {
	public static final boolean[] keys = new boolean[65536];
	private static double scrollDeltaX;
	private static double scrollDeltaY;
	private static double mousePositionX;
	private static double mousePositionY;
	private static double lastMousePositionX;
	private static double lastMousePositionY;
	
	public static class Keyboard extends GLFWKeyCallback {
		@Override
		public void invoke(long window, int key, int scancode, int action, int mods) {
			if (key < 0 || key >= keys.length) return;
			keys[key] = (action != GLFW.GLFW_RELEASE);
		}
	}
	
	public static class MousePosition extends GLFWCursorPosCallback {
		@Override
		public void invoke(long window, double xpos, double ypos) {
			mousePositionX = xpos;
			mousePositionY = ypos;
		}
	}
	
	public static class MouseScroll extends GLFWScrollCallback {
		@Override
		public void invoke(long window, double xoffset, double yoffset) {
			scrollDeltaX = xoffset;
			scrollDeltaY = yoffset;
		}
	}
	
	/**
	 * Calling this method will reset all the internal delta values.
	 */
	public static void process() {
		scrollDeltaX = 0;
		scrollDeltaY = 0;
		
		// TODO: In some instances this has created a "glitch" where the cursor is
		//       moved when toggling between capture and non capture modes
		lastMousePositionX = mousePositionX;
		lastMousePositionY = mousePositionY;
	}
	
	public static boolean pollKey(int key) {
		if (key < 0 || key >= keys.length) return false;
		boolean pressed = keys[key];
		keys[key] = false;
		return pressed;
	}
	
	public static double getScrollDeltaX() {
		return scrollDeltaX;
	}
	
	public static double getScrollDeltaY() {
		return scrollDeltaY;
	}
	
	public static double getMouseX() {
		return mousePositionX;
	}
	
	public static double getMouseY() {
		return mousePositionY;
	}
	
	public static double getMouseDeltaX() {
		return mousePositionX - lastMousePositionX;
	}
	
	public static double getMouseDeltaY() {
		return mousePositionY - lastMousePositionY;
	}
}
