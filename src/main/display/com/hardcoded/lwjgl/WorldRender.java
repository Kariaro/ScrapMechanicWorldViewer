package com.hardcoded.lwjgl;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.opengl.GL11;

import com.hardcoded.asset.ScrapMechanicAssetHandler;
import com.hardcoded.game.World;
import com.hardcoded.logger.Log;
import com.hardcoded.lwjgl.cache.*;
import com.hardcoded.lwjgl.gui.Gui;
import com.hardcoded.lwjgl.mesh.BlockMesh;
import com.hardcoded.lwjgl.render.RenderPipeline;
import com.hardcoded.lwjgl.shader.BlockShader;
import com.hardcoded.lwjgl.shader.TileShader;
import com.hardcoded.lwjgl.shadow.ShadowFrameBuffer;
import com.hardcoded.lwjgl.shadow.ShadowShader;
import com.hardcoded.sm.objects.BodyList.ChildShape;
import com.hardcoded.sm.objects.BodyList.RigidBody;
import com.hardcoded.world.utils.ShapeUtils;
import com.hardcoded.world.utils.ShapeUtils.Bounds3D;

/**
 * The world render class.
 * 
 * @author HardCoded
 * @since v0.1
 */
public class WorldRender {
	private static final Log LOGGER = Log.getLogger();
	
	public static final float NEAR_PLANE = 0.01f;
	public static final float FOV = 70;
	
	public static int height;
	public static int width;
	
	private final LwjglWorldViewer parent;
	private final long window;
	
	private WorldContentHandler worldHandler;
	
	public Camera camera;
	
	private List<RigidBody> bodies = new ArrayList<>();
	private World world;
	private Gui gui;
	
	private RenderPipeline renderPipeline;
	
	public WorldRender(LwjglWorldViewer parent, long window, int width, int height) {
		this.parent = parent;
		this.window = window;
		
		camera = new Camera(window);
		gui = new Gui(this);
		setViewport(width, height);
		
		worldHandler = new WorldContentHandler();
		
		this.renderPipeline = new RenderPipeline(worldHandler, camera);
		
		camera.x = -1750;
		camera.y = -1660;
		camera.z = 10;
		
//		camera.x = 0;
//		camera.y = 0;
//		camera.z = 0;
		
		// ASG
		// r: alpha
		// g: specular level
		// b: glow
		// a: reflectivity
		
		try {
			//checkWorldUpdate();
			init();
		} catch(Exception e) {
			LOGGER.throwing(e);
			throw new RuntimeException("Failed to load sql or compile shaders");
		}
	}
	
	public WorldAssetCache getAssetRender(UUID uuid) {
		return worldHandler.getAssetCache(uuid);
	}
	
	public WorldHarvestableCache getHarvestableRender(UUID uuid) {
		return worldHandler.getHarvestableCache(uuid);
	}
	
	public WorldPartCache getPartRender(UUID uuid) {
		return worldHandler.getPartCache(uuid);
	}
	
	public WorldBlockCache getBlockRender(UUID uuid) {
		return worldHandler.getBlockCache(uuid);
	}
	
	public WorldTileCache getTileRender(int x, int y) {
		return worldHandler.getTileCache(x, y);
	}
	
	public static int updates = 0;
	public static long lastTimed = 0;
	
	
	// TODO: This is a little cheat to reload files in realtime.
	//public static final String fileName = "Survival/Amazing World.db";
	//public static final String fileName = "Survival/TESTINGSQLITE.db";
	//public static final String fileName = "TestingSQLite.db";
	//public static final String fileName = "TestingRotationStuff.db";
	public static final String fileName = "Survival/Amazing World.db";
	private long last = -1;
	private void checkWorldUpdate() {
		File originPath = new File(ScrapMechanicAssetHandler.$USER_DATA, "Save/" + fileName);
		
		long now = originPath.lastModified();
		if(last != now && last + 1000 < System.currentTimeMillis()) {
			last = now;
			/*
			if(lastTimed == 0) {
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
			
//			try {
//				File targetPath = new File("res/clone/", fileName);
//				
//				// Copy world from game path to local path.
//				if(!originPath.canRead()) {
//					// Could not do this action
//					return;
//				}
//				//copyPath.delete();
//				
//				Files.copy(originPath.toPath(), targetPath.toPath(), StandardCopyOption.REPLACE_EXISTING);
//				
//				world = World.loadWorld(targetPath);
//				bodies.clear();
//				bodies = world.getBodyList().getAllRigidBodies();
//				
//				world.close();
//				
//				renderPipeline.loadWorld(world);
//			} catch(Exception e) {
//				LOGGER.error("Failed to load world file");
//				LOGGER.throwing(e);
//				//throw new Error("Failed to load world file");
//			}
			
			try {
				File targetPath = new File("res/backup/first-bup1.db");
				
				world = World.loadWorld(targetPath);
				bodies.clear();
				bodies = world.getBodyList().getAllRigidBodies();
				
				world.close();
				
				renderPipeline.loadWorld(world);
			} catch(Exception e) {
				LOGGER.error("Failed to load world file");
				LOGGER.throwing(e);
				//throw new Error("Failed to load world file");
			}
		}
	}
	
	public void setViewport(int width, int height) {
		WorldRender.height = height;
		WorldRender.width = width;
		gui.width = width;
		gui.height = height;
		
		GL11.glViewport(0, 0, width, height);
		GL11.glLoadIdentity();
		GL11.glMatrixMode(GL11.GL_PROJECTION_MATRIX);
		GL11.glOrtho(0, width, height, 0, 1, -1);
		GL11.glMatrixMode(GL11.GL_MODELVIEW_MATRIX);
	}
	
	protected BlockShader blockShader;
	protected TileShader tileShader;
	protected ShadowShader shadowShader;
	protected ShadowFrameBuffer frameBuffer;
	
	private void init() {
		// Instantiate the block model
		WorldBlockCache.mesh = new BlockMesh();
		
		worldHandler.init();
		renderPipeline.init();
		renderPipeline.loadPipelines();
		
		blockShader = worldHandler.blockShader;
		tileShader = worldHandler.tileShader;
		shadowShader = worldHandler.shadowShader;
		frameBuffer = worldHandler.frameBuffer;
		
		checkWorldUpdate();
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
		GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
		GL11.glClearColor(0.369f, 0.784f, 0.886f, 1);
		
		worldHandler.setLoadLimit(-1);
		Matrix4f projectionView = camera.getProjectionMatrix(FOV, width, height);
		Matrix4f viewMatrix = camera.getViewMatrix();
		
		//projectionView = new Matrix4f(mvpMatrix).translate(0, 0, -camera.z);
		GL11.glEnable(GL_DEPTH_TEST);
		GL11.glEnable(GL_CULL_FACE);
		GL11.glEnable(GL_TEXTURE_2D);
//		Matrix4f lightDirection = new Matrix4f();
//		{
//			float angle = (float)Math.toRadians((System.currentTimeMillis() % 7200L) / 20.0f);
//			lightDirection.rotateX(angle);
//		}
		
//		Light light_test = new Light();
//		light_test.setColor(1, 1, 1);
//		light_test.setPosition(-1747, -1643, 10);
//		partShader.loadLights(List.of(light_test), viewMatrix);
		
		renderPipeline.load(viewMatrix, projectionView);
		renderPipeline.render();
		
//		if(false) {
//			GL11.glPushMatrix();
//			GL11.glLoadMatrixf(projectionView.get(new float[16]));
//			//debugRenders();
//			
//			// Center of world
//			renderCube(
//				0.25f, 0.25f, 0.25f, 0.5f, 0.5f, 0.5f,
//				0x20ffffff
//			);
//			
//			GL11.glPopMatrix();
//		}
		
		GL11.glDisable(GL_DEPTH_TEST);
		GL11.glDisable(GL_CULL_FACE);
		GL11.glColor4f(1, 1, 1, 1);
		
		gui.render();
	}
	
	public void debugRenders() {
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
		boolean SHOW_BOUNDS = false;
		if(SHOW_AABB) {
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
					0.5f,
					
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
			
			if(SHOW_BOUNDS) {
				for(RigidBody body : bodies) {
					Bounds3D bounds = ShapeUtils.getBoundingBox(body);
					Vector3f middle = bounds.getMiddle();
					
//					boolean has = false;
//					for(ChildShape shape : body.shapes) {
//						if(shape.uuid.toString().equals("c0159b96-edf3-46cd-9fbe-96ee1126304b")) {
//							has = true;
//							break;
//						}
//					}
//					
//					if(!has) continue;
					Matrix4f matrix = new Matrix4f();
					
					{
						if(body.isGridLocked_0_2 == 2) {
							matrix.rotate(body.quat);
						} else {
							if(body.staticFlags < -1) {
								matrix.rotate(body.quat);
							}
						}
						matrix.translateLocal(body.xWorld, body.yWorld, body.zWorld);
						matrix.translate(middle.x, middle.y, middle.z);
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
			}
			
			GL11.glDisable(GL11.GL_BLEND);
			GL11.glDisable(GL11.GL_ALPHA);
			
			GL11.glEnable(GL_DEPTH_TEST);
			for(RigidBody body : bodies) {
				boolean has = false;
				for(ChildShape shape : body.shapes) {
					if(shape.uuid.toString().equals("c0159b96-edf3-46cd-9fbe-96ee1126304b")) {
						has = true;
						break;
					}
				}
				
				if(!has) continue;
				Bounds3D bounds = ShapeUtils.getBoundingBox(body);
				Vector3f middle = bounds.getMiddle();
				
				renderCube(
					0.5f + body.yMin,
					0.5f + body.xMin,
					0.5f,
					
					-middle.x * 2,
					-middle.y * 2,
					-middle.z * 2,
					
					0x7f7f7f7f
				);
			}
		}
	}
}