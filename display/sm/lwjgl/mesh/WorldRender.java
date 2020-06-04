package sm.lwjgl.mesh;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Logger;

import org.joml.Matrix4f;
import org.lwjgl.opengl.GL11;

import sm.lwjgl.Camera;
import sm.lwjgl.LwjglWorldViewer;
import sm.lwjgl.gui.Gui;
import sm.lwjgl.input.Input;
import sm.lwjgl.shader.BlockShader;
import sm.lwjgl.shader.PartShader;
import sm.objects.BodyList.ChildShape;
import sm.objects.BodyList.RigidBody;
import sm.world.Block;
import sm.world.Part;
import sm.world.World;

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
			updateBodies();
			init();
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	public void updateBodies() {
		try {
			//world = World.loadWorld("C:\\Users\\Admin\\AppData\\Roaming\\Axolot Games\\Scrap Mechanic\\User\\User_76561198251506208\\Save\\TestingSQLite.db");
			world = World.loadWorld("C:\\Users\\Admin\\AppData\\Roaming\\Axolot Games\\Scrap Mechanic\\User\\User_76561198251506208\\Save\\Survival\\Amazing World.db");
			bodies.clear();
			bodies = world.getSaveFile().getBodyList().getAllRigidBodies();
			world.getSaveFile().close();
		} catch(Exception e) {
			LOGGER.severe("Failed to load world file");
			e.printStackTrace();
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
		
		for(Block block : world.getAllBlocks()) {
			System.out.println("Init: " + block);
			blocks.put(block.uuid, new WorldBlockRender(block, blockShader));
		}
		
		for(Part part : world.getAllParts()) {
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
		
		if(xs < 1) xs = 0.5f;
		if(ys < 1) ys = 0.5f;
		if(zs < 1) zs = 0.5f;
		
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
		
		blockShader.bind();
		blockShader.setUniform("projectionView", projectionTran);
		blockShader.setUniform("transformationMatrix", new Matrix4f());
		blockShader.setUniform("colors", 1, 1, 1, 1);
		
		for(RigidBody body : bodies) {
			for(ChildShape shape : body.shapes) {
				WorldBlockRender mesh = blocks.getOrDefault(shape.uuid_11_16, null);
				
				if(mesh != null) {
					mesh.render(shape);
				}
			}
		}
		
		blockShader.unbind();
		
		
		partShader.bind();
		blockShader.setUniform("projectionView", projectionTran);
		blockShader.setUniform("transformationMatrix", new Matrix4f());
		blockShader.setUniform("colors", 1, 1, 1, 1);
		for(RigidBody body : bodies) {
			for(ChildShape shape : body.shapes) {
				WorldPartRender mesh = parts.getOrDefault(shape.uuid_11_16, null);
				
				if(mesh != null) {
					mesh.render(shape);
				}
			}
		}
		partShader.unbind();
		
		GL11.glPushMatrix();
		GL11.glLoadMatrixf(projectionTran.get(new float[16]));
		for(RigidBody body : bodies) {
			for(ChildShape shape : body.shapes) {
				if(blocks.containsKey(shape.uuid_11_16)) {
					continue;
				}
				if(parts.containsKey(shape.uuid_11_16)) {
					continue;
				}
				
				//if(true) continue;
				//render(shape);
				renderCube(
					shape.yPos_33_2,
					shape.zPos_35_2,
					shape.xPos_31_2,
					
					shape.ys_43_2,
					shape.zs_45_2,
					shape.xs_41_2,
					
					
					0x20ffffff//shape.color_37_4
				);
			}
		}
		
		if(Input.pollKey(GLFW_KEY_O)) {
			updateBodies();
		}
		
		GL11.glPopMatrix();
		GL11.glDisable(GL_DEPTH_TEST);
		GL11.glDisable(GL_CULL_FACE);
		
		gui.render();
	}
}