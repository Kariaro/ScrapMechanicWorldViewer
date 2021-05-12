package com.hardcoded.lwjgl.render;

import java.lang.Math;
import java.util.List;
import java.util.UUID;

import org.joml.*;

import com.hardcoded.lwjgl.Camera;
import com.hardcoded.lwjgl.WorldRender;
import com.hardcoded.lwjgl.mesh.TileMesh;
import com.hardcoded.lwjgl.shader.AssetShader;
import com.hardcoded.lwjgl.shader.TileShader;
import com.hardcoded.math.Quat;
import com.hardcoded.math.Vec3;
import com.hardcoded.tile.Tile;
import com.hardcoded.tile.impl.TilePart;
import com.hardcoded.tile.object.Asset;

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
	
	private Tile tile;
	@SuppressWarnings("unused")
	private String path;
	private TileMesh tm;
	
	private int x;
	private int y;
	private int r;
	
	public WorldTileRender(WorldRender render, int r, int x, int y, String path, Tile tile, TileShader tileShader, AssetShader assetShader) {
		this.render = render;
		this.path = path;
		this.tile = tile;
		this.x = x;
		this.y = y;
		this.r = r;
		
		this.tm = new TileMesh(tile);
		this.tileShader = tileShader;
		this.assetShader = assetShader;
	}
	
	public void render(int ofx, int ofy, Matrix4f projectionTran, Camera camera) {
		if(tile == null) {
			// Bad tile
			return;
		}
		
		int tx = (ofx + this.x) * 64 * 4;
		int ty = (ofy + this.y) * 64 * 4;
		Matrix4f rott = new Matrix4f();
		rott.translateLocal(-128, -128, 0).rotateLocalZ(r * (float)(Math.PI / 2.0)).translateLocal(128, 128, 0);
		
		Matrix4f transform = new Matrix4f(rott);
		transform.translateLocal(tx, ty, 0);
		transform.scale(4);
		
		tileShader.bind();
		tileShader.setUniform("projectionView", projectionTran);
		tileShader.setUniform("transformationMatrix", transform);
		tm.render();
		tileShader.unbind();
		
		assetShader.bind();
		assetShader.setUniform("projectionView", projectionTran);
		assetShader.setUniform("transformationMatrix", transform);
		assetShader.setUniform("color", 1, 1, 1, 1);
		
		for(int y = 0; y < tile.getHeight(); y++) {
			for(int x = 0; x < tile.getWidth(); x++) {
				TilePart part = tile.getPart(x, y);
				Vector3f part_offset = new Vector3f(x * 64 + tx, y * 64 + ty, 0);
				
				for(int i = 0; i < 4; i++) {
					List<Asset> list = part.assets[i];
					
					for(Asset asset : list) {
						UUID uuid = asset.getUUID();
						Vec3 pos = asset.getPosition();
						Quat rot = asset.getRotation();
						Vec3 scl = asset.getSize();
						
						WorldAssetRender mesh = render.getAssetRender(uuid);
						if(mesh != null) {
							Vector3f a = new Vector3f(pos.x * 4, pos.y * 4, pos.z * 4);
							a.add(-128, -128, 0).rotateZ(r * (float)(Math.PI / 2.0)).add(128, 128, 0);
							Vector3f vec_pos = a.add(part_offset);
							
							mesh.render(
								vec_pos,
								new Quaternionf(rot.x, rot.y, rot.z, rot.w),
								new Vector3f(scl.x * 4, scl.y * 4, scl.z * 4),
								camera
							);
						}
					}
				}
			}
		}
		
		assetShader.unbind();
	}
}
