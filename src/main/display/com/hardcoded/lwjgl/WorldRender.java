package com.hardcoded.lwjgl;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;

import com.hardcoded.asset.ScrapMechanicAssetHandler;
import com.hardcoded.game.World;
import com.hardcoded.logger.Log;
import com.hardcoded.lwjgl.gui.Gui;
import com.hardcoded.lwjgl.meshrender.TileTestRender;
import com.hardcoded.lwjgl.render.*;
import com.hardcoded.lwjgl.shader.*;
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
	
	private TileTestRender tileTestRender;
	
	public WorldRender(LwjglWorldViewer parent, long window, int width, int height) {
		this.parent = parent;
		this.window = window;
		
		camera = new Camera(window);
		gui = new Gui(this);
		setViewport(width, height);
		
		worldHandler = new WorldContentHandler();
		tileTestRender = new TileTestRender(worldHandler, camera);
		
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
	
	public WorldAssetRender getAssetRender(UUID uuid) {
		return worldHandler.getAssetRender(uuid);
	}
	
	public WorldHarvestableRender getHarvestableRender(UUID uuid) {
		return worldHandler.getHarvestableRender(uuid);
	}
	
	public WorldPartRender getPartRender(UUID uuid) {
		return worldHandler.getPartRender(uuid);
	}
	
	public WorldBlockRender getBlockRender(UUID uuid) {
		return worldHandler.getBlockRender(uuid);
	}
	
	public WorldTileRender getTileRender(int x, int y) {
		return worldHandler.getTileRender(x, y);
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
			
			try {
				File targetPath = new File("res/clone/", fileName);
				
				// Copy world from game path to local path.
				if(!originPath.canRead()) {
					// Could not do this action
					return;
				}
				//copyPath.delete();
				
				Files.copy(originPath.toPath(), targetPath.toPath(), StandardCopyOption.REPLACE_EXISTING);
				
				world = World.loadWorld(targetPath);
				bodies.clear();
				bodies = world.getBodyList().getAllRigidBodies();
				
				world.close();
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
	protected AssetShader assetShader;
	protected PartShader partShader;
	protected TileShader tileShader;
	protected ShadowShader shadowShader;
	protected ShadowFrameBuffer frameBuffer;
	
	private void init() {
		worldHandler.init();
		tileTestRender.init();
		
		blockShader = worldHandler.blockShader;
		assetShader = worldHandler.assetShader;
		partShader = worldHandler.partShader;
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
	
	
	private Matrix4f getOrthoProjectionMatrix(float width, float height, float length) {
		Matrix4f matrix = new Matrix4f();
		matrix.m00( 2f / width);
		matrix.m11( 2f / height);
		matrix.m22(-2f / length);
		matrix.m33(1);
		
		return matrix;
	}
	
	private int last_mvp_x;
	private int last_mvp_y;
	public void render() {
		GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
		GL11.glClearColor(0.369f, 0.784f, 0.886f, 1);
		
		worldHandler.setLoadLimit(-1);
		
		Matrix4f projectionView = camera.getProjectionMatrix(FOV, width, height);
		Matrix4f viewMatrix = camera.getViewMatrix();
		
		Matrix4f mvpMatrix = getOrthoProjectionMatrix(500, 500, 400);
		int mvp_x = -((int)(camera.x / 64)) * 64;
		int mvp_y = -((int)(camera.y / 64)) * 64;
		
		//projectionView = new Matrix4f(mvpMatrix).translate(0, 0, -camera.z);
		GL11.glEnable(GL_DEPTH_TEST);
		GL11.glEnable(GL_CULL_FACE);
		GL11.glEnable(GL_TEXTURE_2D);
		
		{
			float angle = (float)Math.toRadians((System.currentTimeMillis() % 7200L) / 20.0f);
			mvpMatrix.rotateLocalX(1.0f);
			//mvpMatrix.rotateZ(angle);
			mvpMatrix.translate(mvp_x, mvp_y, -70);
		}
		
		// We only need to calculate the shadows when we move
		if(last_mvp_x != mvp_x || last_mvp_y != mvp_y) {
			last_mvp_x = mvp_x;
			last_mvp_y = mvp_y;
			
			GL11.glPushMatrix();
			tryRenderShadows(projectionView, mvpMatrix);
			GL11.glPopMatrix();
		}
		GL11.glEnable(GL_CULL_FACE);
		
		Matrix4f lightDirection = new Matrix4f();
		{
			float angle = (float)Math.toRadians((System.currentTimeMillis() % 7200L) / 20.0f);
			lightDirection.rotateX(angle);
		}
		
//		Light light_test = new Light();
//		light_test.setColor(1, 1, 1);
//		light_test.setPosition(-1747, -1643, 10);
//		partShader.loadLights(List.of(light_test), viewMatrix);
		
		Matrix4f toShadowSpace = createOffset().mul(mvpMatrix);
		{
			GL13.glActiveTexture(GL13.GL_TEXTURE9);
			GL11.glBindTexture(GL11.GL_TEXTURE_2D, frameBuffer.getShadowMap());
		}
		
		tileTestRender.set(toShadowSpace, viewMatrix, projectionView, lightDirection);
		tileTestRender.render(camera.getPosition(), 3);
		
		partShader.bind();
		partShader.setProjectionView(projectionView);
		partShader.setViewMatrix(viewMatrix);
		partShader.setModelMatrix(new Matrix4f());
		partShader.setShadowMapSpace(toShadowSpace);
		partShader.setLightDirection(lightDirection);
		for(RigidBody body : bodies) {
			for(ChildShape shape : body.shapes) {
				WorldPartRender mesh = getPartRender(shape.uuid);
				
				if(mesh != null) {
					mesh.render(shape);
				}
			}
		}
		partShader.unbind();
		
		blockShader.bind();
		blockShader.setLightDirection(lightDirection);
		blockShader.setProjectionView(projectionView);
		blockShader.setViewMatrix(viewMatrix);
		blockShader.setModelMatrix(new Matrix4f());
		blockShader.setShadowMapSpace(toShadowSpace);
		for(RigidBody body : bodies) {
			for(ChildShape shape : body.shapes) {
				WorldBlockRender mesh = getBlockRender(shape.uuid);
				
				if(mesh != null) {
					mesh.render(shape);
				}
			}
		}
		blockShader.unbind();
		
		GL11.glPushMatrix();
		GL11.glLoadMatrixf(projectionView.get(new float[16]));
		//debugRenders();
		
		// Center of world
		renderCube(
			0.25f, 0.25f, 0.25f, 0.5f, 0.5f, 0.5f,
			0x20ffffff
		);
		
		GL11.glPopMatrix();
		
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
	
	public void tryRenderShadows(Matrix4f projectionTran, Matrix4f mvpMatrix) {
		frameBuffer.bindFrameBuffer();
		GL11.glClear(GL11.GL_COLOR_BUFFER_BIT);
		GL11.glClear(GL11.GL_DEPTH_BUFFER_BIT);
		GL11.glEnable(GL11.GL_DEPTH_TEST);
		
		shadowShader.bind();
		shadowShader.setMvpMatrix(mvpMatrix);
		{
			int ss = 4;
			
			Vector3f cam_pos = camera.getPosition();
			int xx = (int)(cam_pos.x / 64);
			int yy = (int)(cam_pos.y / 64);
			
			for(int y = yy - ss - 1; y < yy + ss; y++) {
				for(int x = xx - ss - 1; x < xx + ss; x++) {
					WorldTileRender render = getTileRender(x, y);
					if(render != null) {
						render.renderShadows(shadowShader, x, y, mvpMatrix);
					}
				}
			}
			
			{
				for(RigidBody body : bodies) {
					for(ChildShape shape : body.shapes) {
						WorldPartRender part_mesh = getPartRender(shape.uuid);
						if(part_mesh == null) continue;
						Matrix4f matrix = new Matrix4f(mvpMatrix);
						
						Matrix4f mat = part_mesh.calculateMatrix(shape);
						matrix.mul(mat);
						
						shadowShader.setMvpMatrix(matrix);
						// TODO: It's important to apply the material. LIGHT_PASS etc
						part_mesh.render(shape);
					}
					
					for(ChildShape shape : body.shapes) {
						WorldBlockRender block_mesh = getBlockRender(shape.uuid);
						if(block_mesh == null) continue;
						
						Matrix4f matrix = new Matrix4f(mvpMatrix);
						
						{
							float x = shape.xPos;
							float y = shape.yPos;
							float z = shape.zPos;
							Matrix4f mat = new Matrix4f();
							
							{
								if(shape.body.isGridLocked_0_2 == 2) {
									mat.rotate(body.quat);
								} else {
									if(body.staticFlags < -1) {
										mat.rotate(body.quat);
									}
								}
								mat.translateLocal(body.xWorld, body.yWorld, body.zWorld);
								mat.scale(1 / 4.0f);
								mat.translate(x, y, z);
								mat.scale(shape.xSize, shape.ySize, shape.zSize);
							}
							
							matrix.mul(mat);
						}

						shadowShader.setMvpMatrix(matrix);
						block_mesh.renderShadows();
					}
				}
			}
		}
		
		shadowShader.unbind();
		frameBuffer.unbindFrameBuffer();
		
		GL11.glEnable(GL_TEXTURE_2D);
		GL11.glDisable(GL_CULL_FACE);
		
//		GL11.glPushMatrix();
//		GL11.glLoadMatrixf(projectionTran.get(new float[16]));
//		GL11.glTranslatef(0, 0, 100f);
//		GL11.glTranslatef((int)(camera.x / 64) * 64, (int)(camera.y / 64) * 64, 0);
//		
//		GL11.glColor4f(1, 1, 1, 1);
//		GL20.glActiveTexture(GL20.GL_TEXTURE0);
//		GL11.glBindTexture(GL11.GL_TEXTURE_2D, frameBuffer.getShadowMap());
//		GL11.glBegin(GL11.GL_TRIANGLE_FAN);
//			GL11.glTexCoord2f(0, 0); GL11.glVertex2f(-250, -250);
//			GL11.glTexCoord2f(1, 0); GL11.glVertex2f( 250, -250);
//			GL11.glTexCoord2f(1, 1); GL11.glVertex2f( 250,  250);
//			GL11.glTexCoord2f(0, 1); GL11.glVertex2f(-250,  250);
//		GL11.glEnd();
//		GL11.glPopMatrix();
		
		GL11.glEnable(GL_CULL_FACE);
	}
	
	public static Matrix4f createOffset() {
		return new Matrix4f()
			.translate(0.5f, 0.5f, 0.5f)
			.scale(0.5f, 0.5f, 0.5f);
	}
}