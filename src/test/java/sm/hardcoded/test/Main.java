package sm.hardcoded.test;

import java.awt.Window.Type;
import java.awt.image.BufferedImage;
import java.io.File;
import java.lang.reflect.Field;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;

import com.hardcoded.tile.Tile;
import com.hardcoded.tile.TileReader;

public class Main {

	public static void main(String[] args) {
		String path = getTile("tile_12");
		// path = getTile("tile_9");
		// path = getTile("tile_5");
		// path = getTile("tile_1");
		// path = getTile("TESTING_TILE_FLAT");
		// path = getTile("TESTING_TILE_FLAT_MEDIUM");
		// path = getTile("TESTING_TILE_FLAT_MEDIUM_COLORFLAT_2");
		// path = getTile("TESTING_TILE_FLAT_MEDIUM_COLORFLAT");
		
		//path = getGameTile("MEADOW128_09");
		path = getGameTile("HILLS512_01");
		//path = getGameTile("MEADOW256_01");
		path = getGameTile("GROUND512_01");
		path = getTile("tile_test");
		// path = "res/testing/TestHideout.tile";
		//path = "D:\\Steam\\steamapps\\common\\Scrap Mechanic\\Survival\\Terrain\\Tiles\\start_area\\SurvivalStartArea_BigRuin_01.tile";
		
		
		Tile tile = null;
		try {
			tile = TileReader.readTile(path);
		} catch(Exception e) {
			e.printStackTrace();
		}
		
		debug(tile);
	}
	
	
	private static String getTile(String name) {
		File tile_path = new File("C:/Users/Admin/AppData/Roaming/Axolot Games/Scrap Mechanic/User/User_76561198251506208/Tiles/");
		
		for(File dir_file : tile_path.listFiles()) {
			for(File file : dir_file.listFiles()) {
				if(file.getName().equals(name + ".tile")) return file.getAbsolutePath();
			}
		}
		
		return null;
	}
	
	private static String getGameTile(String name) {
		File tile_path = new File("D:/Steam/steamapps/common/Scrap Mechanic/Data/Terrain/Tiles/");
		
		for(File file : tile_path.listFiles()) {
			if(file.getName().equals(name + ".tile")) return file.getAbsolutePath();
		}
		
		return null;
	}
	
	private static void testFull() {
		File tile_path = new File("D:/Steam/steamapps/common/Scrap Mechanic/Data/Terrain/Tiles/");
		
		for(File file : tile_path.listFiles()) {
			if(file.getName().endsWith(".tile")) {
				try {
					// loadTile(file.getAbsolutePath());
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		
	}
	
	private static void debug(Tile tile) {
		int tileHeight = tile.getHeight();
		int tileWidth = tile.getWidth();
		int m = 8;
		
		BufferedImage bi1 = new BufferedImage(0x20 * tileWidth + 1, 0x20 * tileHeight + 1, BufferedImage.TYPE_INT_ARGB);
		BufferedImage bi2 = new BufferedImage(0x20 * tileWidth + 1, 0x20 * tileHeight + 1, BufferedImage.TYPE_INT_ARGB);
		BufferedImage bi3 = new BufferedImage(0x80 * tileWidth, 0x80 * tileHeight, BufferedImage.TYPE_INT_ARGB);
		BufferedImage bi4 = new BufferedImage(0x40 * tileWidth + 1, 0x40 * tileHeight + 1, BufferedImage.TYPE_INT_ARGB);
		
		int[] colors = getObject(tile, "vertexColor");
		bi1.setRGB(0, 0, bi1.getWidth(), bi1.getHeight(),
			colors, 0, bi1.getWidth()
		);
		
		float[] heights = getObject(tile, "vertexHeight");
		for(int i = 0; i < colors.length; i++) {
			colors[i] = ((int)(0x7f + 0x10 * heights[i]) * 0x010101) | 0xff000000;
		}
		bi2.setRGB(0, 0, bi2.getWidth(), bi2.getHeight(),
			colors, 0, bi2.getWidth()
		);
		
		byte[] clutters = getObject(tile, "clutter");
		colors = new int[bi3.getWidth() * bi3.getHeight()];
		for(int i = 0; i < colors.length; i++) {
			colors[i] = ((0x3423415 * Byte.toUnsignedInt(clutters[i])) & 0xffffff) | 0xff000000;
			// int value = Byte.toUnsignedInt(clutters[i]);
			// System.out.println(value);
			// colors[i] = (0x010101 * ((value * 0x3423415) & 0xffffff)) | 0xff000000;
		}
		bi3.setRGB(0, 0, bi3.getWidth(), bi3.getHeight(),
			colors, 0, bi3.getWidth()
		);

		long[] ground = getObject(tile, "ground");
		colors = new int[bi4.getWidth() * bi4.getHeight()];
		for(int i = 0; i < colors.length; i++) {
			int xx = i % bi4.getWidth();
			int yy = i / bi4.getWidth();
			long value = ground[xx + (bi4.getHeight() - yy - 1) * bi4.getHeight()];
			int c0 = (int)((value >> 48L) & 0xffff) * 0x3232124;
			int c1 = (int)((value >> 32L) & 0xffff) * 0x172f323;
			int c2 = (int)((value >> 16L) & 0xffff) * 0x4305123;
			int c3 = (int)((value       ) & 0xffff) * 0xff83f23;
			int cccc = ((c0 ^ c1 ^ c2 ^ c3) & 0xffffff) | 0xff000000;
			colors[i] = cccc;
		}
		bi4.setRGB(0, 0, bi4.getWidth(), bi4.getHeight(),
			colors, 0, bi4.getWidth()
		);
		
		createWindow(bi1, "ColorMap", m);
		createWindow(bi2, "HeightMap", m);
		createWindow(bi3, "Clutter", m / 2);
		createWindow(bi4, "MaterialMap", m);
	}
	
	@SuppressWarnings("unchecked")
	private static <T> T getObject(Object obj, String fieldName) {
		Class<?> clazz = obj.getClass();
		
		try {
			Field field = clazz.getDeclaredField(fieldName);
			boolean old = field.isAccessible();
			field.setAccessible(true);
			Object result = field.get(obj);
			field.setAccessible(old);
			
			return (T) result;
		} catch(Exception e) {
			e.printStackTrace();
		}
		
		return null;
	}
	
	private static void createWindow(BufferedImage bi, String name, int m) {
		BufferedImage bi2 = new BufferedImage(bi.getWidth() * m, bi.getWidth() * m, BufferedImage.TYPE_INT_ARGB);
		bi2.createGraphics().drawImage(bi, 0, 0, bi2.getWidth(), bi2.getHeight(), null);
		
		JFrame frame = new JFrame(name);
		frame.setResizable(false);
		frame.setSize(500, 500);
		frame.setType(Type.UTILITY);
		frame.setAlwaysOnTop(true);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		JLabel label = new JLabel(new ImageIcon(bi2));
		label.setBorder(null);
		frame.getContentPane().add(label);
		frame.setVisible(true);
		frame.pack();
	}
}
