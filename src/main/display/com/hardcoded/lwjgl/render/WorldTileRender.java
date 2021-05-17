package com.hardcoded.lwjgl.render;

import java.util.List;
import java.util.UUID;

import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import com.hardcoded.lwjgl.*;
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
	private WorldContentHandler handler;
	
	private AssetShader assetShader;
	private TileShader tileShader;
	
	private TileParts parts;
	
	public WorldTileRender(WorldContentHandler handler, int x, int y, TileParts parts, TileShader tileShader, AssetShader assetShader) {
		this.handler = handler;
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
	
	public void render(int x, int y, Matrix4f toShadowSpace, Matrix4f viewMatrix, Matrix4f projectionView, Camera camera) {
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
		tileShader.setProjectionView(projectionView);
		tileShader.setModelMatrix(transform);
		tileShader.setShadowMapSpace(toShadowSpace);
		tm.render();
		tileShader.unbind();
		
		assetShader.bind();
		assetShader.setProjectionView(projectionView);
		assetShader.setViewMatrix(viewMatrix);
		assetShader.setModelMatrix(transform);
		assetShader.setShadowMapSpace(toShadowSpace);
		
		Vector3f part_offset = new Vector3f(tile_x, tile_y, tile_z);
		for(int i = 0; i < 4; i++) {
			List<Harvestable> harvestables = part.harvestables[i];
			List<Asset> assets = part.assets[i];
			
			for(Asset asset : assets) {
				UUID uuid = asset.getUuid();
				Vec3 apos = asset.getPosition();
				Quat arot = asset.getRotation();
				Vec3 _size = asset.getSize();
				
				WorldAssetRender mesh = handler.getAssetRender(uuid);
				if(mesh != null) {
					Vector3f vec_pos = new Vector3f(apos.toArray()).add(-32, -32, 0).rotateZ(rot_offset).add(32, 32, 0).add(part_offset);
					//float dist = camera.getPosition().distance(pos);
					
					Matrix4f modelMatrix = new Matrix4f()
						//.translate(part_offset).rotateZ(rot_offset).translate(apos.getX(), apos.getY(), apos.getZ())
						.translate(vec_pos)
						.rotate(new Quaternionf(arot.getX(), arot.getY(), arot.getZ(), arot.getW()).rotateLocalZ(rot_offset))
						.scale(_size.getX(), _size.getY(), _size.getZ());
					
					assetShader.setModelMatrix(modelMatrix);
					mesh.render(asset);
				}
			}
			
			for(Harvestable harvestable : harvestables) {
				UUID uuid = harvestable.getUuid();
				Vec3 apos = harvestable.getPosition();
				Quat arot = harvestable.getRotation();
				Vec3 _size = harvestable.getSize();
				
				WorldHarvestableRender mesh = handler.getHarvestableRender(uuid);
				if(mesh != null) {
					Vector3f vec_pos = new Vector3f(apos.toArray()).add(-32, -32, 0).rotateZ(rot_offset).add(32, 32, 0).add(part_offset);
					//float dist = camera.getPosition().distance(pos);
					
					Matrix4f modelMatrix = new Matrix4f()
						//.translate(part_offset).rotateZ(rot_offset).translate(apos.getX(), apos.getY(), apos.getZ())
						.translate(vec_pos)
						.rotate(new Quaternionf(arot.getX(), arot.getY(), arot.getZ(), arot.getW()).rotateLocalZ(rot_offset))
						.scale(_size.getX(), _size.getY(), _size.getZ());
					
					assetShader.setModelMatrix(modelMatrix);
					mesh.render(harvestable);
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
			
			shader.setMvpMatrix(tmt);
			tm.renderShadows();
		}
		
		Vector3f part_offset = new Vector3f(tile_x, tile_y, tile_z);
		for(int i = 0; i < 4; i++) {
			for(Asset asset : part.assets[i]) {
				UUID uuid = asset.getUuid();
				Vec3 apos = asset.getPosition();
				Quat arot = asset.getRotation();
				Vec3 _size = asset.getSize();
				
				WorldAssetRender mesh = handler.getAssetRender(uuid);
				if(mesh != null) {
					Vector3f vec_pos = new Vector3f(apos.toArray()).add(-32, -32, 0).rotateZ(rot_offset).add(32, 32, 0).add(part_offset);
					//float dist = camera.getPosition().distance(pos);
					
					Matrix4f matrix = new Matrix4f(mvpMatrix)
						.translate(vec_pos)
						.rotate(new Quaternionf(arot.getX(), arot.getY(), arot.getZ(), arot.getW()).rotateLocalZ(rot_offset))
						.scale(_size.getX(), _size.getY(), _size.getZ());
					
					shader.setMvpMatrix(matrix);
					mesh.renderShadows();
				}
			}
			
			for(Harvestable harvestable : part.harvestables[i]) {
				UUID uuid = harvestable.getUuid();
				Vec3 apos = harvestable.getPosition();
				Quat arot = harvestable.getRotation();
				Vec3 _size = harvestable.getSize();
				
				WorldHarvestableRender mesh = handler.getHarvestableRender(uuid);
				if(mesh != null) {
					Vector3f vec_pos = new Vector3f(apos.toArray()).add(-32, -32, 0).rotateZ(rot_offset).add(32, 32, 0).add(part_offset);
					//float dist = camera.getPosition().distance(pos);
					
					Matrix4f matrix = new Matrix4f(mvpMatrix)
						.translate(vec_pos)
						.rotate(new Quaternionf(arot.getX(), arot.getY(), arot.getZ(), arot.getW()).rotateLocalZ(rot_offset))
						.scale(_size.getX(), _size.getY(), _size.getZ());
					
					shader.setMvpMatrix(matrix);
					mesh.renderShadows();
				}
			}
		}
	}
}
