package me.hardcoded.smviewer.lwjgl.data;

import me.hardcoded.smreader.utils.NotNull;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

/**
 * Image loading utility class
 */
public class ImageLoader {
	public static BufferedImage loadResourceImage(String path) {
		InputStream stream = ImageLoader.class.getResourceAsStream(path);
		if (stream == null) {
			return null;
		}
		
		BufferedImage bi;
		try {
			return ImageIO.read(stream);
		} catch (IOException e) {
			// TODO: Logging
			e.printStackTrace();
		}
		
		return null;
	}
	
	public static ByteBuffer loadResourceBuffer(String path) {
		return createBuffer(loadResourceImage(path));
	}
	
	public static ByteBuffer createBuffer(BufferedImage bi) {
		if (bi == null) {
			return null;
		}
		
		int height = bi.getHeight();
		int width = bi.getWidth();
		
		boolean hasAlpha = (bi.getTransparency() == Transparency.TRANSLUCENT);
		
		ByteBuffer buf = ByteBuffer.allocateDirect(4 * width * height);
		int[] pixels = new int[width * height];
		bi.getRGB(0, 0, width, height, pixels, 0, width);
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				int pos;
				if (false) {
					pos = (height - y - 1) * width + x;
				} else {
					pos = y * width + x;
				}
				
				int pixel = pixels[pos];
				int r = (pixel >> 16) & 0xff;
				int g = (pixel >>  8) & 0xff;
				int b = (pixel      ) & 0xff;
				
				buf.put((byte)r);
				buf.put((byte)g);
				buf.put((byte)b);
				
				if (hasAlpha) {
					buf.put((byte)((pixel >> 24) & 0xff));
				} else {
					buf.put((byte)0xff);
				}
			}
		}
		buf.flip();
		
		return buf;
	}
}
