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
	
	private TilePart part;
	private TileMesh tm;
	
	private int x;
	private int y;
	private int rot;
	
	public WorldTileRender(WorldRender render, int x, int y, int ox, int oy, TileParts parts, TileShader tileShader, AssetShader assetShader) {
		this.render = render;
		this.x = x;
		this.y = y;
		this.rot = TileData.getTileRotation(x, y);

		TilePart part = parts.getPart(ox, oy);
		TileMesh mesh = parts.getMesh(ox, oy);
		this.part = part;
		this.tm = mesh;
		this.tileShader = tileShader;
		this.assetShader = assetShader;
	}
	
	public void render(int ofx, int ofy, Matrix4f projectionTran, Camera camera) {
		if(part == null) {
			// Bad part
			return;
		}
		
		int tx = (ofx + this.x) * 64;
		int ty = (ofy + this.y) * 64;
		
		Matrix4f rott = new Matrix4f();
		//int rot = 0;
		float rot_offset = rot * (float)(Math.PI / 2.0);
		rott.translateLocal(-32, -32, 0).rotateLocalZ(rot_offset).translateLocal(32, 32, 0);

		float c_sw = TileData.getTileCliffLevel(x    , y    );
		float c_se = TileData.getTileCliffLevel(x + 1, y    );
		float c_nw = TileData.getTileCliffLevel(x    , y + 1);
		float c_ne = TileData.getTileCliffLevel(x + 1, y + 1);
		float cliff_level = Math.min(Math.min(c_sw, c_se), Math.min(c_nw, c_ne)) * 8;
		
		float tz = cliff_level;
		Matrix4f transform = new Matrix4f(rott);
		transform.translateLocal(tx, ty, tz);
		
		tileShader.bind();
		tileShader.setUniform("projectionView", projectionTran);
		tileShader.setUniform("transformationMatrix", transform);
		tm.render();
		tileShader.unbind();
		
		assetShader.bind();
		assetShader.setUniform("projectionView", projectionTran);
		assetShader.setUniform("transformationMatrix", transform);
		assetShader.setUniform("color", 1, 1, 1, 1);
		
		Vector3f part_offset = new Vector3f(tx, ty, tz);
		
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
						vec_pos,
						asset,
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
}
