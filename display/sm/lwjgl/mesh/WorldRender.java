package sm.lwjgl.mesh;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.joml.Matrix4f;
import org.joml.Vector4f;
import org.lwjgl.opengl.GL11;

import sm.asset.ScrapMechanicAssets;
import sm.lwjgl.Camera;
import sm.lwjgl.LwjglWorldViewer;
import sm.lwjgl.gui.Gui;
import sm.lwjgl.shader.BlockShader;
import sm.lwjgl.shader.PartShader;
import sm.objects.BodyList.ChildShape;
import sm.objects.BodyList.RigidBody;
import sm.objects.BodyList.RigidBodyBounds;
import sm.world.World;
import sm.world.types.Block;
import sm.world.types.Part;
import sm.world.types.ShapeUtils;
import sm.world.types.ShapeUtils.Bounds3D;

public class WorldRender {
	private static final Logger LOGGER = Logger.getLogger(WorldRender.class.getName());
	
	private final LwjglWorldViewer parent;
	private final long window;
	private int height;
	private int width;
	
	private BlockShader blockShader;
	private PartShader partShader;
	public Camera camera;
	private Gui gui;
	
	private World world;
	private List<RigidBody> bodies = new ArrayList<>();
	private Map<UUID, WorldBlockRender> blocks;
	private Map<UUID, WorldPartRender> parts;
	
	public WorldRender(LwjglWorldViewer parent, long window, int width, int height) {
		this.parent = parent;
		this.window = window;
		blocks = new HashMap<>();
		parts = new HashMap<>();
		
		camera = new Camera(window);
		gui = new Gui(this);
		setViewport(width, height);
		
		try {
			checkWorldUpdate();
			init();
		} catch(Exception e) {
			e.printStackTrace();
			throw new RuntimeException("Failed to load sql or compile shaders");
		}
	}
	
	// TODO: This is a little cheat to reload files in realtime.
	//public static final String fileName = "Survival/Amazing World.db";
	public static final String fileName = "TestingSQLite.db";
	//public static final String fileName = "SQLiteRotations.db";
	private long last = -1;
	private void checkWorldUpdate() {
		File filePath = new File(World.$USER_DATA, "Save/" + fileName);
		
		long now = filePath.lastModified();
		if(last != now) {
			last = now;
			
			try {
				File copyPath = new File("res/clone/", fileName);
				
				// Copy world from game to local path.
				Files.copy(Paths.get(filePath.toURI()), Paths.get(copyPath.toURI()), StandardCopyOption.REPLACE_EXISTING);
				
				//world = World.loadWorld("res/world/WT_10MDG+24TGD_2020.db");
				
				world = World.loadWorld(copyPath);
				bodies.clear();
				bodies = world.getSaveFile().getBodyList().getAllRigidBodies();
				world.getSaveFile().close();
			} catch(Exception e) {
				LOGGER.severe("Failed to load world file");
				e.printStackTrace();
				
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
	
	private void init() throws Exception {
		blockShader = new BlockShader();
		partShader = new PartShader();
	}
	
	private WorldBlockRender getBlockRender(UUID uuid) {
		if(blocks.containsKey(uuid)) return blocks.get(uuid);
		
		Block block = ScrapMechanicAssets.getBlock(uuid);
		if(block == null) return null;
		
		WorldBlockRender render = new WorldBlockRender(block, blockShader);
		blocks.put(block.uuid, render);
		return render;
	}
	
	private WorldPartRender getPartRender(UUID uuid) {
		if(parts.containsKey(uuid)) return parts.get(uuid);
		
		Part part = ScrapMechanicAssets.getPart(uuid);
		if(part == null) return null;
		
		LOGGER.log(Level.INFO, "Init: {0}", part);
		WorldPartRender render = new WorldPartRender(part, partShader);
		parts.put(part.uuid, render);
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
		GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
		Matrix4f projectionView = camera.getProjectionViewMatrix(60, width, height);
		Matrix4f projectionTran = camera.getProjectionMatrix(60, width, height);
		Matrix4f viewMatrix = camera.getViewMatrix(60, width, height);
		
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
			// TODO: This should be cached inside the body!
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
			// TODO: This should be cached inside the body!
			Bounds3D bounds = ShapeUtils.getBoundingBox(body);
			
			for(ChildShape shape : body.shapes) {
				WorldPartRender mesh = getPartRender(shape.uuid);
				
				if(mesh != null) {
					mesh.render(shape, bounds);
				}
			}
		}
		partShader.unbind();
		
		GL11.glPushMatrix();
		GL11.glLoadMatrixf(projectionTran.get(new float[16]));
		for(RigidBody body : bodies) {
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
		}
		
		boolean SHOW_AABB = true;
		if(SHOW_AABB) {
			GL11.glDisable(GL_DEPTH_TEST);
			GL11.glPolygonMode(GL11.GL_FRONT_AND_BACK, GL11.GL_LINE);
			for(RigidBody body : bodies) {
				float ys = body.yMax - body.yMin;
				float xs = body.xMin - body.xMax;

				if(body.bodyId != 1557) continue;
				renderCube(
					body.yMin * 4 + 0.5f,
					body.xMax * 4 + 0.5f,
					0 + 0.5f,
					
					ys * 4,
					xs * 4,
					0,
					
					0x20ffffff
				);
			}
			
			GL11.glPolygonMode(GL11.GL_FRONT_AND_BACK, GL11.GL_FILL);
			for(RigidBody body : bodies) {
				RigidBodyBounds bounds = body.bounds;
				if(body.isStatic_0_2 == 2) {
					float xm = (bounds.xMax + bounds.xMin) * 2;
					float ym = (bounds.yMax + bounds.yMin) * 2;
					
					Vector4f right = body.matrix.getColumn(0, new Vector4f()).mul(3);
					Vector4f at = body.matrix.getColumn(1, new Vector4f()).mul(3);
					Vector4f up = body.matrix.getColumn(2, new Vector4f()).mul(3);
					
					GL11.glPushMatrix();
					GL11.glBegin(GL_LINES);
						GL11.glColor3f(1, 0, 0);
						GL11.glVertex3f(xm, ym, 0.5f);
						GL11.glVertex3f(right.x + xm, right.y + ym, right.z + 0.5f);
						
						GL11.glColor3f(0, 1, 0);
						GL11.glVertex3f(xm, ym, 0.5f);
						GL11.glVertex3f(up.x + xm, up.y + ym, up.z + 0.5f);
						
						GL11.glColor3f(0, 0, 1);
						GL11.glVertex3f(xm, ym, 0.5f);
						GL11.glVertex3f(at.x + xm, at.y + ym, at.z + 0.5f);
					GL11.glEnd();
					GL11.glPopMatrix();
				}
			}
			
			GL11.glEnable(GL11.GL_BLEND);
			GL11.glEnable(GL11.GL_ALPHA);
			GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
			for(RigidBody body : bodies) {
				Bounds3D bounds = ShapeUtils.getBoundingBox(body);
				
				renderCube(
					bounds.xMin + 0.5f,
					bounds.yMin + 0.5f,
					bounds.zMin + 0.5f,
					
					bounds.xMax - bounds.xMin,
					bounds.yMax - bounds.yMin,
					bounds.zMax - bounds.zMin,
					
					0x7fff0000
				);
			}
			GL11.glDisable(GL11.GL_BLEND);
			GL11.glDisable(GL11.GL_ALPHA);
			
			for(RigidBody body : bodies) {
				Bounds3D bounds = ShapeUtils.getBoundingBox(body);
				//if(body.bodyId != 1531) continue;
				renderCube(
					(bounds.xMin + bounds.xMax) / 2.0f,
					(bounds.yMin + bounds.yMax) / 2.0f,
					(bounds.zMin + bounds.zMax) / 2.0f,
					
					1, 1, 1,
					
					0xff0000ff
				);
			}
		}
		

		GL11.glEnable(GL_DEPTH_TEST);
		
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