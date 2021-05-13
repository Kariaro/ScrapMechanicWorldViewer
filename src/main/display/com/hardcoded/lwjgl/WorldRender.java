package com.hardcoded.lwjgl;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.*;

import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.opengl.GL11;

import com.hardcoded.asset.ScrapMechanicAssetHandler;
import com.hardcoded.db.types.*;
import com.hardcoded.error.TileException;
import com.hardcoded.game.World;
import com.hardcoded.logger.Log;
import com.hardcoded.lwjgl.gui.Gui;
import com.hardcoded.lwjgl.render.*;
import com.hardcoded.lwjgl.shader.*;
import com.hardcoded.sm.objects.BodyList.ChildShape;
import com.hardcoded.sm.objects.BodyList.RigidBody;
import com.hardcoded.sm.objects.TileData;
import com.hardcoded.tile.Tile;
import com.hardcoded.tile.TileReader;
import com.hardcoded.world.utils.ShapeUtils;
import com.hardcoded.world.utils.ShapeUtils.Bounds3D;

public class WorldRender {
	private static final Log LOGGER = Log.getLogger();
	
	private final LwjglWorldViewer parent;
	private final long window;
	private int height;
	private int width;
	
	private BlockShader blockShader;
	private AssetShader assetShader;
	private PartShader partShader;
	private TileShader tileShader;
	
	public Camera camera;
	private Gui gui;
	
	private World world;
	private List<RigidBody> bodies = new ArrayList<>();
	private Map<UUID, WorldAssetRender> assets;
	private Map<UUID, WorldBlockRender> blocks;
	private Map<UUID, WorldPartRender> parts;
	private Map<String, TileParts> tile_data;
	private Map<Long, WorldTileRender> tiles;
	
	public WorldRender(LwjglWorldViewer parent, long window, int width, int height) {
		this.parent = parent;
		this.window = window;
		assets = new HashMap<>();
		blocks = new HashMap<>();
		parts = new HashMap<>();
		
		tiles = new HashMap<>();
		tile_data = new HashMap<>();
		
		camera = new Camera(window);
		gui = new Gui(this);
		setViewport(width, height);
		
		camera.x = -1750;
		camera.y = -1660;
		camera.z = 40;
		
		try {
			//checkWorldUpdate();
			init();
		} catch(Exception e) {
			LOGGER.throwing(e);
			throw new RuntimeException("Failed to load sql or compile shaders");
		}
	}
	
	//private TileMesh tm;
	public static int updates = 0;
	public static long lastTimed = 0;
	
	
	// TODO: This is a little cheat to reload files in realtime.
	//public static final String fileName = "Survival/Amazing World.db";
	//public static final String fileName = "Survival/TESTINGSQLITE.db";
	//public static final String fileName = "TestingSQLite.db";
	public static final String fileName = "Survival/Amazing World.db";
	private long last = -1;
	private void checkWorldUpdate() {
		File filePath = new File(ScrapMechanicAssetHandler.$USER_DATA, "Save/" + fileName);
		
		long now = filePath.lastModified();
		if(last != now) {
			//System.out.println("Hello??????? Testing?????");
			/*if(lastTimed == 0) {
				lastTimed = System.currentTimeMillis();
			} else {
				if(System.currentTimeMillis() - lastTimed > 1000) {
					lastTimed += 1000;
					System.out.println(updates);
					updates = 0;
				}
			}
			updates ++;
			*/
			try {
				File copyPath = new File("res/clone/", fileName);
				
				// Copy world from game path to local path.
				if(!filePath.canRead()) {
					// Could not do this action
					return;
				}
				//copyPath.delete();
				
				Files.copy(filePath.toPath(), copyPath.toPath(), StandardCopyOption.REPLACE_EXISTING);
				//FileUtils.copy(filePath, copyPath, StandardCopyOption.REPLACE_EXISTING);
				
				world = World.loadWorld(copyPath);
				bodies.clear();
				bodies = world.getBodyList().getAllRigidBodies();
				
				// TODO: Try to keep the connection open?
				world.close();
				last = now;
			} catch(Exception e) {
				LOGGER.error("Failed to load world file");
				LOGGER.throwing(e);
				throw new Error("Failed to load world file");
			}
		}
	}
	
	public void setViewport(int width, int height) {
		this.height = height;
		this.width = width;
		gui.width = width;
		gui.height = height;
		
		GL11.glViewport(0, 0, width, height);
		GL11.glLoadIdentity();
		GL11.glMatrixMode(GL11.GL_PROJECTION_MATRIX);
		GL11.glOrtho(0, width, height, 0, 1, -1);
		GL11.glMatrixMode(GL11.GL_MODELVIEW_MATRIX);
	}
	
//	private static String getTile(String name) {
//		File tile_path = new File("C:/Users/Admin/AppData/Roaming/Axolot Games/Scrap Mechanic/User/User_76561198251506208/Tiles/");
//		
//		for(File dir_file : tile_path.listFiles()) {
//			for(File file : dir_file.listFiles()) {
//				if(file.getName().equals(name + ".tile")) return file.getAbsolutePath();
//			}
//		}
//		
//		return null;
//	}
	
//	private static String getGameTile(String name) {
//		File tile_path = new File("D:/Steam/steamapps/common/Scrap Mechanic/Data/Terrain/Tiles/ClassicCreativeTiles/");
//		
//		for(File file : tile_path.listFiles()) {
//			if(file.getName().equals(name + ".tile")) return file.getAbsolutePath();
//		}
//		
//		return null;
//	}
	
	private Tile loaded_tile;
	private void init() throws Exception {
		blockShader = new BlockShader();
		assetShader = new AssetShader();
		partShader = new PartShader();
		tileShader = new TileShader();
		
		//String path = "GROUND512_01";
		//path = getTile("tile_0");
		//path = getGameTile("GROUND512_01");
		//path = getGameTile("HILLS512_01");
		//path = "res/testing/TestHideout.tile";
		//tm = new TileMesh(TileReader.read("D:/Steam/steamapps/common/Scrap Mechanic/Data/Terrain/Tiles/" + name + ".tile"));
		
		//this.loaded_tile = TileReader.loadTile(path);
		//tm = new TileMesh(loaded_tile);
	}
	
	private int item_load;
	public WorldBlockRender getBlockRender(UUID uuid) {
		if(blocks.containsKey(uuid)) return blocks.get(uuid);
		
		SMBlock block = ScrapMechanicAssetHandler.getBlock(uuid);
		if(block == null) return null;
		if(item_load-- < 0) return null;
		
		WorldBlockRender render = new WorldBlockRender(block, blockShader);
		blocks.put(block.uuid, render);
		return render;
	}
	
	public WorldPartRender getPartRender(UUID uuid) {
		if(parts.containsKey(uuid)) return parts.get(uuid);
		
		SMPart part = ScrapMechanicAssetHandler.getPart(uuid);
		if(part == null) return null;
		if(item_load-- < 0) return null;
		
		LOGGER.info("Init: %s", part);
		WorldPartRender render = new WorldPartRender(part, partShader);
		parts.put(part.uuid, render);
		return render;
	}
	
	public WorldAssetRender getAssetRender(UUID uuid) {
		if(assets.containsKey(uuid)) return assets.get(uuid);
		
		SMAsset asset = ScrapMechanicAssetHandler.getAsset(uuid);
		if(asset == null) return null;
		if(item_load-- < 0) return null;
		
		LOGGER.info("Init: %s", asset);
		WorldAssetRender render = new WorldAssetRender(asset, assetShader);
		assets.put(asset.uuid, render);
		return render;
	}
	
	public WorldTileRender getTileRender(int x, int y) {
		long index = ((long)(x) & 0xffffffffL) | (((long)y) << 32L);
		if(tiles.containsKey(index)) return tiles.get(index);

		String path = TileData.getTilePath(x, y);
		if(path == null) return null;
		
		if(item_load-- < 0) return null;
		
		int ox = TileData.getTileOffsetX(x, y);
		int oy = TileData.getTileOffsetY(x, y);
		//long o_index = ((long)(ox) & 0xffffffffL) | (((long)oy) << 32L);
		
		TileParts parts = null;
		if(tile_data.containsKey(path)) {
			parts = tile_data.get(path);
		} else {
			try {
				Tile tile = TileReader.loadTile(path);
				parts = new TileParts(tile);
				tile_data.put(path, parts);
			} catch(TileException e) {
				LOGGER.throwing(e);
			} catch(IOException e) {
				LOGGER.throwing(e);
			}
		}
		
		LOGGER.info("Init: '%s'", path);
		WorldTileRender render = new WorldTileRender(this, x, y, ox, oy, parts, tileShader, assetShader);
		tiles.put(index, render);
		return render;
	}
	
	public int getFps() {
		return parent.getFps();
	}
	
	public void update() {
		glfwSwapBuffers(window);
		glfwPollEvents();
		camera.update();
	}
	
	private void renderCube(float x, float y, float z, float xs, float ys, float zs, int rgba) {
		float rc, gc, bc, ac;
		{
			ac = ((rgba >> 24) & 0xff) / 255.0f;
			rc = ((rgba >> 16) & 0xff) / 255.0f;
			gc = ((rgba >>  8) & 0xff) / 255.0f;
			bc = ((rgba      ) & 0xff) / 255.0f;
		}
		
		//if(xs < 1) xs = 0.5f;
		//if(ys < 1) ys = 0.5f;
		//if(zs < 1) zs = 0.5f;
		
		x -= 0.5f;
		y -= 0.5f;
		z -= 0.5f;
		GL11.glBegin(GL11.GL_TRIANGLE_FAN);
			GL11.glColor4f(1, 1, 1, 0.5f);
			GL11.glVertex3f(x     , y + ys, z);
			GL11.glColor4f(rc, gc, bc, ac);
			GL11.glVertex3f(x + xs, y + ys, z);
			GL11.glVertex3f(x + xs, y     , z);
			GL11.glVertex3f(x     , y     , z);
		GL11.glEnd();
		
		GL11.glBegin(GL11.GL_TRIANGLE_FAN);
			GL11.glColor4f(1, 1, 0, 0.5f);
			GL11.glVertex3f(x + xs, y + ys, z     );
			GL11.glColor4f(rc, gc, bc, ac);
			GL11.glVertex3f(x + xs, y + ys, z + zs);
			GL11.glVertex3f(x + xs, y     , z + zs);
			GL11.glVertex3f(x + xs, y     , z     );
		GL11.glEnd();
		
		GL11.glBegin(GL11.GL_TRIANGLE_FAN);
			GL11.glColor4f(1, 0, 1, 0.5f);
			GL11.glVertex3f(x     , y     , z + zs);
			GL11.glColor4f(rc, gc, bc, ac);
			GL11.glVertex3f(x + xs, y     , z + zs);
			GL11.glVertex3f(x + xs, y + ys, z + zs);
			GL11.glVertex3f(x     , y + ys, z + zs);
		GL11.glEnd();
		
		GL11.glBegin(GL11.GL_TRIANGLE_FAN);
			GL11.glColor4f(0, 1, 1, 0.5f);
			GL11.glVertex3f(x, y + ys, z + zs);
			GL11.glColor4f(rc, gc, bc, ac);
			GL11.glVertex3f(x, y + ys, z     );
			GL11.glVertex3f(x, y     , z     );
			GL11.glVertex3f(x, y     , z + zs);
		GL11.glEnd();
		
		GL11.glBegin(GL11.GL_TRIANGLE_FAN);
			GL11.glColor4f(1, 0, 0, 0.5f);
			GL11.glVertex3f(x     , y + ys, z     );
			GL11.glColor4f(rc, gc, bc, ac);
			GL11.glVertex3f(x     , y + ys, z + zs);
			GL11.glVertex3f(x + xs, y + ys, z + zs);
			GL11.glVertex3f(x + xs, y + ys, z     );
		GL11.glEnd();
		
		GL11.glBegin(GL11.GL_TRIANGLE_FAN);
			GL11.glColor4f(0, 0, 1, 0.5f);
			GL11.glVertex3f(x     , y, z + zs);
			GL11.glColor4f(rc, gc, bc, ac);
			GL11.glVertex3f(x     , y, z     );
			GL11.glVertex3f(x + xs, y, z     );
			GL11.glVertex3f(x + xs, y, z + zs);
		GL11.glEnd();
	}
	
	public void render() {
		item_load = 1;
		GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
		//Matrix4f projectionView = camera.getProjectionViewMatrix(60, width, height);
		Matrix4f projectionTran = camera.getProjectionMatrix(70, width, height);
		//Matrix4f viewMatrix = camera.getViewMatrix(60, width, height);
		
		GL11.glEnable(GL_DEPTH_TEST);
		GL11.glEnable(GL_CULL_FACE);
		GL11.glEnable(GL_TEXTURE_2D);
		
		checkWorldUpdate();
		
		blockShader.bind();
		blockShader.setUniform("projectionView", projectionTran);
		blockShader.setUniform("transformationMatrix", new Matrix4f());
		blockShader.setUniform("cameraDirection", camera.getViewDirection());
		blockShader.setUniform("color", 1, 1, 1, 1);
		for(RigidBody body : bodies) {
			Bounds3D bounds = ShapeUtils.getBoundingBox(body);
			
			for(ChildShape shape : body.shapes) {
				WorldBlockRender mesh = getBlockRender(shape.uuid);

				if(mesh != null) {
					mesh.render(shape, bounds);
				}
			}
		}
		blockShader.unbind();
		
		partShader.bind();
		partShader.setUniform("projectionView", projectionTran);
		partShader.setUniform("transformationMatrix", new Matrix4f());
		for(RigidBody body : bodies) {
			Bounds3D bounds = ShapeUtils.getBoundingBox(body);
			
			//System.out.println("Body: " + body.bodyId);
			for(ChildShape shape : body.shapes) {
				WorldPartRender mesh = getPartRender(shape.uuid);
				
				if(mesh != null) {
					mesh.render(shape, bounds, camera);
				}
			}
		}
		partShader.unbind();
		
		{
			// correct = (-2270, -2567, 10)
			// current = (-1676, -1500, 1)
			
			int ox = 0;
			int oy = 0;
			int ss = 1;
			
			Vector3f cam_pos = camera.getPosition();
			int xx = (int)(cam_pos.x / 64) - ox;
			int yy = (int)(cam_pos.y / 64) - oy;
			
			for(int y = yy - ss - 1; y <= yy + ss; y++) {
				for(int x = xx - ss - 1; x <= xx + ss; x++) {
					WorldTileRender render = getTileRender(x, y);
					if(render != null) {
						render.render(ox, oy, projectionTran, camera);
					}
				}
			}
		}
		
		
		GL11.glPushMatrix();
		GL11.glLoadMatrixf(projectionTran.get(new float[16]));
		/*for(RigidBody body : bodies) {
			//System.out.println(body.bodyId);
			for(ChildShape shape : body.shapes) {
				if(blocks.containsKey(shape.uuid)) continue;
				if(parts.containsKey(shape.uuid)) continue;
				
				renderCube(
					shape.xPos + 0.5f,
					shape.yPos + 0.5f,
					shape.zPos + 0.5f,
					
					1, 1, 1,
					
					0x20ffffff
				);
			}
		}*/
		
		boolean SHOW_AABB = true;
		if(SHOW_AABB) {
			// Show location in the world of the aabb
			GL11.glPolygonMode(GL11.GL_FRONT_AND_BACK, GL11.GL_LINE);
			for(RigidBody body : bodies) {
				float ys = body.yMax - body.yMin;
				float xs = body.xMin - body.xMax;

//				boolean has = false;
//				for(ChildShape shape : body.shapes) {
//					if(shape.uuid.toString().equals("c0159b96-edf3-46cd-9fbe-96ee1126304b")) {
//						has = true;
//						break;
//					}
//				}
				
				//if(!has) continue;
				renderCube(
					body.yMin + 0.5f,
					body.xMax + 0.5f,
					0 + 0.5f,
					
					ys,
					xs,
					0,
					
					0x20ffffff
				);
			}
			
			GL11.glLineWidth(1.75f);
			GL11.glDisable(GL_DEPTH_TEST);
			GL11.glPolygonMode(GL11.GL_FRONT_AND_BACK, GL11.GL_FILL);
			GL11.glEnable(GL11.GL_BLEND);
			GL11.glEnable(GL11.GL_ALPHA);
			GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
			
			for(RigidBody body : bodies) {
				Bounds3D bounds = ShapeUtils.getBoundingBox(body);
				Vector3f middle = bounds.getMiddle();
				
//				boolean has = false;
//				for(ChildShape shape : body.shapes) {
//					if(shape.uuid.toString().equals("c0159b96-edf3-46cd-9fbe-96ee1126304b")) {
//						has = true;
//						break;
//					}
//				}
				
				//if(!has) continue;
				Matrix4f matrix = new Matrix4f();
				
				if(body.isStatic_0_2 == 2) {
					matrix.translateLocal(
						middle.x + body.xWorld,
						middle.y + body.yWorld,
						middle.z + body.zWorld
					);
					
					matrix.rotateAroundLocal(body.quat,
						body.xWorld,
						body.yWorld,
						body.zWorld
					);
				} else {
					matrix.translateLocal(
						middle.x + body.xWorld,
						middle.y + body.yWorld,
						middle.z + body.zWorld
					);
					
					if(body.staticFlags < -1) {
						matrix.rotateAroundLocal(body.quat,
							body.xWorld,
							body.yWorld,
							body.zWorld
						);
					}
				}
				
				float xm = bounds.xMin - middle.x * 2.0f;
				float ym = bounds.yMin - middle.y * 2.0f;
				float zm = bounds.zMin - middle.z * 2.0f;
				
				GL11.glPushMatrix();
				GL11.glMultMatrixf(matrix.get(new float[16]));
				renderCube(
					bounds.xMin - middle.x + 0.5f,
					bounds.yMin - middle.y + 0.5f,
					bounds.zMin - middle.z + 0.5f,
					
					bounds.xMax - bounds.xMin,
					bounds.yMax - bounds.yMin,
					bounds.zMax - bounds.zMin,
					
					0x7fff0000
				);
				
				float a_len = 0.5f;
				GL11.glBegin(GL_LINES);
					GL11.glColor3f(1, 0, 0);
					GL11.glVertex3f(xm, ym, zm);
					GL11.glVertex3f(xm + a_len, ym, zm);
					
					GL11.glColor3f(0, 1, 0);
					GL11.glVertex3f(xm, ym, zm);
					GL11.glVertex3f(xm, ym, zm + a_len);
					
					GL11.glColor3f(0, 0, 1);
					GL11.glVertex3f(xm, ym, zm);
					GL11.glVertex3f(xm, ym + a_len, zm);
				GL11.glEnd();
				GL11.glPopMatrix();
			}
			
			GL11.glDisable(GL11.GL_BLEND);
			GL11.glDisable(GL11.GL_ALPHA);
			
//			GL11.glEnable(GL_DEPTH_TEST);
//			for(RigidBody body : bodies) {
//				boolean has = false;
//				for(ChildShape shape : body.shapes) {
//					if(shape.uuid.toString().equals("c0159b96-edf3-46cd-9fbe-96ee1126304b")) {
//						has = true;
//						break;
//					}
//				}
//				
//				if(!has) continue;
//				Bounds3D bounds = ShapeUtils.getBoundingBox(body);
//				Vector3f middle = bounds.getMiddle();
//				
//				renderCube(
//					0.5f + body.yMin,
//					0.5f + body.xMin,
//					0.5f,
//					
//					-middle.x * 2,
//					-middle.y * 2,
//					-middle.z * 2,
//					
//					0x7f7f7f7f
//				);
//			}
		}
		
		// Center of world
		renderCube(
			0.25f, 0.25f, 0.25f, 0.5f, 0.5f, 0.5f,
			0x20ffffff
		);
		
		GL11.glPopMatrix();
		GL11.glDisable(GL_DEPTH_TEST);
		GL11.glDisable(GL_CULL_FACE);
		
		gui.render();
	}
}