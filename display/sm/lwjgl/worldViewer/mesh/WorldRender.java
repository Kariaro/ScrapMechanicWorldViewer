package sm.lwjgl.worldViewer.mesh;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import org.joml.Matrix4f;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;

import sm.lwjgl.input.Input;
import sm.lwjgl.worldViewer.Camera;
import sm.lwjgl.worldViewer.LwjglWorldViewer;
import sm.objects.BodyList.ChildShape;
import sm.objects.BodyList.RigidBody;
import sm.util.FileUtils;
import sm.world.World;

public class WorldRender {
	private static final Logger LOGGER = Logger.getLogger(WorldRender.class.getName());
	
	private final LwjglWorldViewer parent;
	private final long window;
	private int height;
	private int width;
	
	private WorldShader shader;
	public Camera camera;
	private Gui gui;
	
	//private TilingMesh background;
	
	private World world;
	private List<RigidBody> bodies = new ArrayList<>();
	
	public WorldRender(LwjglWorldViewer parent, long window, int width, int height) {
		this.parent = parent;
		this.window = window;
		
		camera = new Camera(window);
		gui = new Gui(this);
		setViewport(width, height);
		
		try {
			init();
		} catch(Exception e) {
			e.printStackTrace();
		}
		
		updateBodies();
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
		shader = new WorldShader();
		shader.createShaderCode(FileUtils.readStream(WorldRender.class.getResourceAsStream("/shaders/world_fragment.fs")), GL20.GL_FRAGMENT_SHADER);
		shader.createShaderCode(FileUtils.readStream(WorldRender.class.getResourceAsStream("/shaders/world_vertex.vs")), GL20.GL_VERTEX_SHADER);
		
		shader.bindAttrib(0, "in_Position");
		shader.bindAttrib(1, "in_Uv");
		shader.bindAttrib(2, "in_Color");
		
		
		shader.link();
		shader.createUniform("projectionView");
		shader.createUniform("transformationMatrix");
	}
	
	public int getFps() {
		return parent.getFps();
	}
	
	public void update() {
		glfwSwapBuffers(window);
		glfwPollEvents();
		camera.update();
	}
	
	public void renderCube(float x, float y, float z, int rgba) {
		renderCube(x, y, z, 1, 1, 1, rgba);
	}
	public void renderCube(float x, float y, float z, float xs, float ys, float zs, int rgba) {
		float rc, gc, bc, ac;
		{
			ac = ((rgba >> 24) & 0xff) / 255.0f;
			rc = ((rgba >> 16) & 0xff) / 255.0f;
			gc = ((rgba >>  8) & 0xff) / 255.0f;
			bc = ((rgba      ) & 0xff) / 255.0f;
		}
		
		x -= 0.5f;
		y -= 0.5f;
		z -= 0.5f;
		GL11.glBegin(GL11.GL_TRIANGLE_FAN);
			GL11.glColor3f(1, 1, 1);
			GL11.glVertex3f(x     , y + ys, z);
			GL11.glColor4f(rc, gc, bc, ac);
			GL11.glVertex3f(x + xs, y + ys, z);
			GL11.glVertex3f(x + xs, y     , z);
			GL11.glVertex3f(x     , y     , z);
		GL11.glEnd();
		
		GL11.glBegin(GL11.GL_TRIANGLE_FAN);
			GL11.glColor3f(1, 1, 0);
			GL11.glVertex3f(x + xs, y + ys, z     );
			GL11.glColor4f(rc, gc, bc, ac);
			GL11.glVertex3f(x + xs, y + ys, z + zs);
			GL11.glVertex3f(x + xs, y     , z + zs);
			GL11.glVertex3f(x + xs, y     , z     );
		GL11.glEnd();
		
		GL11.glBegin(GL11.GL_TRIANGLE_FAN);
			GL11.glColor3f(1, 0, 1);
			GL11.glVertex3f(x     , y     , z + zs);
			GL11.glColor4f(rc, gc, bc, ac);
			GL11.glVertex3f(x + xs, y     , z + zs);
			GL11.glVertex3f(x + xs, y + ys, z + zs);
			GL11.glVertex3f(x     , y + ys, z + zs);
		GL11.glEnd();
		
		GL11.glBegin(GL11.GL_TRIANGLE_FAN);
			GL11.glColor3f(0, 1, 1);
			GL11.glVertex3f(x, y + ys, z + zs);
			GL11.glColor4f(rc, gc, bc, ac);
			GL11.glVertex3f(x, y + ys, z     );
			GL11.glVertex3f(x, y     , z     );
			GL11.glVertex3f(x, y     , z + zs);
		GL11.glEnd();
		
		GL11.glBegin(GL11.GL_TRIANGLE_FAN);
			GL11.glColor3f(1, 0, 0);
			GL11.glVertex3f(x     , y + ys, z     );
			GL11.glColor4f(rc, gc, bc, ac);
			GL11.glVertex3f(x     , y + ys, z + zs);
			GL11.glVertex3f(x + xs, y + ys, z + zs);
			GL11.glVertex3f(x + xs, y + ys, z     );
		GL11.glEnd();
		
		GL11.glBegin(GL11.GL_TRIANGLE_FAN);
			GL11.glColor3f(0, 0, 1);
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
		
		GL11.glEnable(GL_DEPTH_TEST);
		shader.bind();
		shader.setUniform("projectionView", projectionView);
		shader.setUniform("transformationMatrix", new Matrix4f());
		shader.unbind();
		
		GL11.glPushMatrix();
		GL11.glLoadMatrixf(projectionTran.get(new float[16]));
		
		for(RigidBody body : bodies) {
			for(ChildShape shape : body.shapes) {
				renderCube(
					shape.yPos_33_2,
					shape.zPos_35_2,
					shape.xPos_31_2,
					
					shape.ys_43_2,
					shape.zs_45_2,
					shape.xs_41_2,
					
					
					shape.color_37_4
				);
			}
		}
		
		if(Input.pollKey(GLFW_KEY_O)) {
			updateBodies();
		}
		
		GL11.glPopMatrix();
		GL11.glDisable(GL_DEPTH_TEST);
		
		gui.render();
	}
}