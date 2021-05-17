package com.hardcoded.lwjgl.shader;

import java.io.File;
import java.io.IOException;

import org.joml.Matrix4f;
import org.lwjgl.opengl.GL11;

import com.hardcoded.asset.ScrapMechanicAssetHandler;
import com.hardcoded.lwjgl.data.Texture;

/**
 * A tile shader implementation.
 * 
 * @author HardCoded
 * @since v0.1
 */
public class TileShader extends ShaderObjectImpl {
	/**
	 * The textures that are stored in this array are the following:
	 * 
	 * <pre>
	 * 0: Default
	 * 1: Concrete
	 * 2: Sand
	 * 3: Stone
	 * 4: Dirt 
	 * 5: Weeds
	 * 6: Rough Stone
	 * 7: Hay
	 * 8: Bright Grass
	 * </pre>
	 */
	public static final Texture[] textures = new Texture[9];
	private static final String[] names = {
		"gnd_grass_dif.tga",
		"gnd_0_dif.tga",
		"gnd_1_dif.tga",
		"gnd_2_dif.tga",
		"gnd_3_dif.tga",
		"gnd_4_dif.tga",
		"gnd_5_dif.tga",
		"gnd_6_dif.tga",
		"gnd_7_dif.tga"
	};
	
	public TileShader() {
		super(
			"/shaders/tile/tile_vertex.vs",
			"/shaders/tile/tile_fragment.fs"
		);
		
		File terrain_path = new File(ScrapMechanicAssetHandler.$GAME_DATA, "Terrain/Textures/Ground/");
		
		try {
			for(int i = 0; i < names.length; i++) {
				String path = new File(terrain_path, names[i]).getAbsolutePath();
				textures[i] = Texture.loadTexture(path, i, GL11.GL_LINEAR);
			}
		} catch(IOException e) {
			throw new ShaderException("Failed to load ground textures");
		}
	}
	
	@Override
	protected void loadBinds() {
		bindAttrib(0, "in_Position");
		bindAttrib(1, "in_Color");
		bindAttrib(2, "in_Uv");
		bindAttrib(3, "in_Material_0");
		bindAttrib(4, "in_Material_1");
		bindAttrib(5, "in_Material_2");
		bindAttrib(6, "in_Material_3");
	}
	
	@Override
	protected void loadUniforms() {
		super.loadUniforms();
		
		createUniform("toShadowMapSpace");
		
		setUniform("tex_0", 0);
		setUniform("tex_1", 1);
		setUniform("tex_2", 2);
		setUniform("tex_3", 3);
		setUniform("tex_4", 4);
		setUniform("tex_5", 5);
		setUniform("tex_6", 6);
		setUniform("tex_7", 7);
		setUniform("tex_8", 8);
		setUniform("shadowMap", 9);
	}
	
	public void setShadowMapSpace(Matrix4f matrix) {
		setUniform("toShadowMapSpace", matrix);
	}
}
