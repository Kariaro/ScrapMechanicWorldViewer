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
import java.util.logging.Logger;

import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;
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
import sm.util.Util;
import sm.world.World;
import sm.world.types.Block;
import sm.world.types.Part;

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
	
	// TODO: This is only for debug
	//private String fileName = "Survival/Amazing World.db";
	//private String fileName = "TestingSQLite.db";
	private String fileName = "SQLiteRotations.db";
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
		
		for(Block block : ScrapMechanicAssets.getAllBlocks()) {
			//System.out.println("Init: " + block);
			blocks.put(block.uuid, new WorldBlockRender(block, blockShader));
		}
		
		for(Part part : ScrapMechanicAssets.getAllParts()) {
			System.out.println("Init: " + part);
			parts.put(part.uuid, new WorldPartRender(part, partShader));
		}
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
		GL11.glEnable(GL11.GL_TEXTURE_2D);
		//GL11.glEnable(GL11.GL_BLEND);
		//GL11.glEnable(GL11.GL_ALPHA);
		//GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
		//shader.bind();
		//shader.setUniform("projectionView", projectionView);
		//shader.setUniform("transformationMatrix", new Matrix4f());
		//shader.unbind();
		
		checkWorldUpdate();
		
		blockShader.bind();
		blockShader.setUniform("projectionView", projectionTran);
		blockShader.setUniform("transformationMatrix", new Matrix4f());
		blockShader.setUniform("cameraDirection", camera.getViewDirection());
		blockShader.setUniform("colors", 1, 1, 1, 1);
		
		for(RigidBody body : bodies) {
			for(ChildShape shape : body.shapes) {
				WorldBlockRender mesh = blocks.getOrDefault(shape.uuid, null);
				
				if(mesh != null) {
					mesh.render(shape);
				}
			}
		}
		
		blockShader.unbind();
		
		
		partShader.bind();
		partShader.setUniform("projectionView", projectionTran);
		partShader.setUniform("transformationMatrix", new Matrix4f());
		
		//blockShader.setUniform("colors", 1, 1, 1, 1);
		for(RigidBody body : bodies) {
			for(ChildShape shape : body.shapes) {
				WorldPartRender mesh = parts.getOrDefault(shape.uuid, null);
				
				if(mesh != null) {
					//mesh.render(shape);
				}
			}
		}
		partShader.unbind();
		
		GL11.glPushMatrix();
		GL11.glLoadMatrixf(projectionTran.get(new float[16]));
		for(RigidBody body : bodies) {
			for(ChildShape shape : body.shapes) {
				if(blocks.containsKey(shape.uuid)) {
					continue;
				}
				/*if(parts.containsKey(shape.uuid)) {
					continue;
				}*/
				
				//if(true) continue;
				//render(shape);
				renderCube(
					shape.xPos + 0.5f,
					shape.yPos + 0.5f,
					shape.zPos + 0.5f,
					
					1,
					1,
					1,
					
					
					0x20ffffff//shape.color_37_4
				);
			}
		}
		
		renderCube(
			0.25f, 0.25f, 0.25f, 0.5f, 0.5f, 0.5f,
			0x20ffffff
		);
		
		boolean SHOW_AABB = true;
		if(SHOW_AABB) {
			GL11.glDisable(GL_DEPTH_TEST);
			GL11.glPolygonMode(GL11.GL_FRONT_AND_BACK, GL11.GL_LINE);
			for(RigidBody body : bodies) {
				float xs = body.xMax - body.xMin;
				float zs = body.zMin - body.zMax;
				renderCube(
					body.xMin * 4 + 0.5f,
					0 + 0.5f,
					body.zMax * 4 + 0.5f,
					
					xs * 4, 0,
					zs * 4,
					
					0x20ffffff
				);
			}
			
			GL11.glPolygonMode(GL11.GL_FRONT_AND_BACK, GL11.GL_FILL);
			for(RigidBody body : bodies) {
				RigidBodyBounds bounds = body.bounds;
				if(body.isStatic_0_2 == 2) {
					float zm = (bounds.xMax + bounds.xMin) * 2;
					float xm = (bounds.zMax + bounds.zMin) * 2;
					
					Vector4f right = body.matrix.getColumn(1, new Vector4f()).mul(3);
					Vector4f up = body.matrix.getColumn(2, new Vector4f()).mul(3);
					Vector4f at = body.matrix.getColumn(0, new Vector4f()).mul(3);
					
					//System.out.println(at.toString(NumberFormat.getNumberInstance()));
					GL11.glPushMatrix();
					GL11.glBegin(GL_LINES);
						GL11.glColor3f(1, 0, 0);
						GL11.glVertex3f(xm, 0.5f, zm);
						GL11.glVertex3f(right.x + xm, right.z + 0.5f, right.y + zm);
						
						GL11.glColor3f(0, 1, 0);
						GL11.glVertex3f(xm, 0.5f, zm);
						GL11.glVertex3f(up.x + xm, up.z + 0.5f, up.y + zm);
						
						GL11.glColor3f(0, 0, 1);
						GL11.glVertex3f(xm, 0.5f, zm);
						GL11.glVertex3f(at.x + xm, at.z + 0.5f, at.y + zm);
					GL11.glEnd();
					GL11.glPopMatrix();
				}
			}
			
			for(RigidBody body : bodies) {
				if(body.isStatic_0_2 == 1) {
					Vector3f middle = body.getMiddleLocal();
					
					renderCube(
						middle.x,
						middle.y,
						middle.z,
						
						1, 1, 1,
						
						0x20ff0000
					);
				}
			}
			
			
		}
		
		GL11.glPopMatrix();
		GL11.glDisable(GL_DEPTH_TEST);
		GL11.glDisable(GL_CULL_FACE);
		
		gui.render();
	}
}