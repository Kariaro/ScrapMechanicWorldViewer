package com.hardcoded.lwjgl.shader;

import java.io.File;

import org.lwjgl.opengl.GL11;

import com.hardcoded.asset.ScrapMechanicAssetHandler;
import com.hardcoded.lwjgl.data.Texture;

public class TileShader extends Shader {
	public static Texture tex_0; // Default
	public static Texture tex_1; // Concrete
	public static Texture tex_2; // Sand
	public static Texture tex_3; // Stone
	public static Texture tex_4; // Dirt 
	public static Texture tex_5; // Weeds
	public static Texture tex_6; // Rough Stone
	public static Texture tex_7; // Hay
	public static Texture tex_8; // Bright Grass
	
	public TileShader() throws Exception {
		super(
			"/shaders/tile/tile_vertex.vs",
			"/shaders/tile/tile_fragment.fs"
		);
		
//		File terrain_path = new File(ScrapMechanicAssetHandler.$GAME_DATA, "Terrain/Textures/Ground/");
//		String nmn = terrain_path.getAbsolutePath() + '\\';
		
		String nmn = "D:/Steam/steamapps/Common/Scrap Mechanic/Data/Terrain/Textures/Ground/";
		tex_0 = Texture.loadTexture(nmn + "gnd_grass_dif.tga", 0, GL11.GL_LINEAR);
		tex_1 = Texture.loadTexture(nmn + "gnd_0_dif.tga", 1, GL11.GL_LINEAR);
		tex_2 = Texture.loadTexture(nmn + "gnd_1_dif.tga", 2, GL11.GL_LINEAR);
		tex_3 = Texture.loadTexture(nmn + "gnd_2_dif.tga", 3, GL11.GL_LINEAR);
		tex_4 = Texture.loadTexture(nmn + "gnd_3_dif.tga", 4, GL11.GL_LINEAR);
		tex_5 = Texture.loadTexture(nmn + "gnd_4_dif.tga", 5, GL11.GL_LINEAR);
		tex_6 = Texture.loadTexture(nmn + "gnd_5_dif.tga", 6, GL11.GL_LINEAR);
		tex_7 = Texture.loadTexture(nmn + "gnd_6_dif.tga", 7, GL11.GL_LINEAR);
		tex_8 = Texture.loadTexture(nmn + "gnd_7_dif.tga", 8, GL11.GL_LINEAR);
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
		createUniform("projectionView");
		createUniform("transformationMatrix");
		createUniform("testColor");
		
		setUniform("tex_0", 0);
		setUniform("tex_1", 1);
		setUniform("tex_2", 2);
		setUniform("tex_3", 3);
		setUniform("tex_4", 4);
		setUniform("tex_5", 5);
		setUniform("tex_6", 6);
		setUniform("tex_7", 7);
		setUniform("tex_8", 8);
		
		setUniform4i("testColor", 0xa98942, 0, 0, 0);
	}
}
