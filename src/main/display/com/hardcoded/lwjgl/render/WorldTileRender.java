package com.hardcoded.lwjgl.render;

import java.util.List;
import java.util.UUID;

import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import com.hardcoded.lwjgl.Camera;
import com.hardcoded.lwjgl.TileParts;
import com.hardcoded.lwjgl.WorldRender;
import com.hardcoded.lwjgl.mesh.TileMesh;
import com.hardcoded.lwjgl.shader.AssetShader;
import com.hardcoded.lwjgl.shader.TileShader;
import com.hardcoded.lwjgl.shadow.ShadowShader;
import com.hardcoded.math.Quat;
import com.hardcoded.math.Vec3;
import com.hardcoded.sm.objects.TileData;
import com.hardcoded.tile.impl.TilePart;
import com.hardcoded.tile.object.Asset;
import com.hardcoded.tile.object.Harvestable;

/**
 * A tile renderer.
 * 
 * @author HardCoded
 * @since v0.1
 */
public class WorldTileRender {
	private WorldRender render;
	
	private AssetShader assetShader;
	private TileShader tileShader;
	
	private TileParts parts;
	
	public WorldTileRender(WorldRender render, int x, int y, TileParts parts, TileShader tileShader, AssetShader assetShader) {
		this.render = render;
		this.parts = parts;
		
//		int ox = TileData.getTileOffsetX(x, y);
//		int oy = TileData.getTileOffsetY(x, y);
//		TilePart part = parts.getPart(ox, oy);
//		TileMesh mesh = parts.getMesh(ox, oy);
		
//		this.part = part;
//		this.tm = mesh;
		this.tileShader = tileShader;
		this.assetShader = assetShader;
	}
	
	public void render(int x, int y, Matrix4f projectionTran, Camera camera) {
		int ox = TileData.getTileOffsetX(x, y);
		int oy = TileData.getTileOffsetY(x, y);
		
		TilePart part = parts.getPart(ox, oy);
		TileMesh tm = parts.getMesh(ox, oy);
		
		float c_sw = TileData.getTileCliffLevel(x    , y    );
		float c_se = TileData.getTileCliffLevel(x + 1, y    );
		float c_nw = TileData.getTileCliffLevel(x    , y + 1);
		float c_ne = TileData.getTileCliffLevel(x + 1, y + 1);
		float cliff_level = Math.min(Math.min(c_sw, c_se), Math.min(c_nw, c_ne)) * 8;
		
		float tile_x = x * 64;
		float tile_y = y * 64;
		float tile_z = cliff_level;
		
		int rot = TileData.getTileRotation(x, y);
		float rot_offset = rot * (float)(Math.PI / 2.0);
		Matrix4f transform = new Matrix4f()
			.translateLocal(-32, -32, 0).rotateLocalZ(rot_offset).translateLocal(32, 32, 0)
			.translateLocal(tile_x, tile_y, tile_z);
		
		tileShader.bind();
		tileShader.setUniform("projectionView", projectionTran);
		tileShader.setUniform("transformationMatrix", transform);
		tm.render();
		tileShader.unbind();
		
		assetShader.bind();
		assetShader.setUniform("projectionView", projectionTran);
		assetShader.setUniform("transformationMatrix", transform);
		assetShader.setUniform("color", 1, 1, 1, 1);
		
		Vector3f part_offset = new Vector3f(tile_x, tile_y, tile_z);
		
		for(int i = 0; i < 4; i++) {
			List<Harvestable> harvestables = part.harvestables[i];
			List<Asset> assets = part.assets[i];
			
			for(Asset asset : assets) {
				UUID uuid = asset.getUuid();
				Vec3 apos = asset.getPosition();
				Quat arot = asset.getRotation();
				Vec3 asze = asset.getSize();
				
				WorldAssetRender mesh = render.getAssetRender(uuid);
				if(mesh != null) {
					Vector3f a = new Vector3f(apos.toArray());
					a.add(-32, -32, 0).rotateZ(rot_offset).add(32, 32, 0);
					Vector3f vec_pos = a.add(part_offset);
					
					mesh.render(
						asset,
						vec_pos,
						new Quaternionf().rotateZ(rot_offset).mul(new Quaternionf(arot.getX(), arot.getY(), arot.getZ(), arot.getW())),
						new Vector3f(asze.toArray()),
						camera
					);
				}
			}
			
			for(Harvestable harvestable : harvestables) {
				UUID uuid = harvestable.getUuid();
				Vec3 apos = harvestable.getPosition();
				Quat arot = harvestable.getRotation();
				Vec3 asze = harvestable.getSize();
				
				WorldHarvestableRender mesh = render.getHarvestableRender(uuid);
				if(mesh != null) {
					Vector3f a = new Vector3f(apos.toArray());
					a.add(-32, -32, 0).rotateZ(rot_offset).add(32, 32, 0);
					Vector3f vec_pos = a.add(part_offset);
					
					mesh.render(
						vec_pos,
						harvestable,
						new Quaternionf().rotateZ(rot_offset).mul(new Quaternionf(arot.getX(), arot.getY(), arot.getZ(), arot.getW())),
						new Vector3f(asze.toArray()),
						camera
					);
				}
			}
		}
		
		assetShader.unbind();
	}
	
	public void renderShadows(ShadowShader shader, int x, int y, Matrix4f mvpMatrix) {
		int ox = TileData.getTileOffsetX(x, y);
		int oy = TileData.getTileOffsetY(x, y);
		
		TilePart part = parts.getPart(ox, oy);
		TileMesh tm = parts.getMesh(ox, oy);
		
		float c_sw = TileData.getTileCliffLevel(x    , y    );
		float c_se = TileData.getTileCliffLevel(x + 1, y    );
		float c_nw = TileData.getTileCliffLevel(x    , y + 1);
		float c_ne = TileData.getTileCliffLevel(x + 1, y + 1);
		float cliff_level = Math.min(Math.min(c_sw, c_se), Math.min(c_nw, c_ne)) * 8;
		
		float tile_x = x * 64;
		float tile_y = y * 64;
		float tile_z = cliff_level;
		
		int rot = TileData.getTileRotation(x, y);
		float rot_offset = rot * (float)(Math.PI / 2.0);
		Matrix4f transform = new Matrix4f()
			.translateLocal(-32, -32, 0).rotateLocalZ(rot_offset).translateLocal(32, 32, 0)
			.translateLocal(tile_x, tile_y, tile_z);
		
		{
			Matrix4f tmt = new Matrix4f(mvpMatrix);
			tmt.mul(transform);
			
			shader.setUniform("mvpMatrix", tmt);
			tm.renderShadows();
		}
		
		Vector3f part_offset = new Vector3f(tile_x, tile_y, tile_z);
		for(int i = 0; i < 4; i++) {
			for(Asset asset : part.assets[i]) {
				UUID uuid = asset.getUuid();
				Vec3 apos = asset.getPosition();
				Quat arot = asset.getRotation();
				Vec3 asze = asset.getSize();
				
				WorldAssetRender mesh = render.getAssetRender(uuid);
				if(mesh != null) {
					Vector3f a = new Vector3f(apos.toArray());
					a.add(-32, -32, 0).rotateZ(rot_offset).add(32, 32, 0);
					Vector3f vec_pos = a.add(part_offset);
					
					Matrix4f matrix = new Matrix4f(mvpMatrix);
					matrix.translate(vec_pos);
					matrix.rotate(new Quaternionf().rotateZ(rot_offset).mul(new Quaternionf(arot.getX(), arot.getY(), arot.getZ(), arot.getW())));
					matrix.scale(new Vector3f(asze.toArray()));
					
					shader.setUniform("mvpMatrix", matrix);
					mesh.renderShadows();
				}
			}
			
			for(Harvestable harvestable : part.harvestables[i]) {
				UUID uuid = harvestable.getUuid();
				Vec3 apos = harvestable.getPosition();
				Quat arot = harvestable.getRotation();
				Vec3 asze = harvestable.getSize();
				
				WorldHarvestableRender mesh = render.getHarvestableRender(uuid);
				if(mesh != null) {
					Vector3f a = new Vector3f(apos.toArray());
					a.add(-32, -32, 0).rotateZ(rot_offset).add(32, 32, 0);
					Vector3f vec_pos = a.add(part_offset);
					
					Matrix4f matrix = new Matrix4f(mvpMatrix);
					matrix.translate(vec_pos);
					matrix.rotate(new Quaternionf().rotateZ(rot_offset).mul(new Quaternionf(arot.getX(), arot.getY(), arot.getZ(), arot.getW())));
					matrix.scale(new Vector3f(asze.toArray()));
					
					shader.setUniform("mvpMatrix", matrix);
					mesh.renderShadows();
				}
			}
		}
	}
}
