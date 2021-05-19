package com.hardcoded.lwjgl.render;

import java.io.IOException;
import java.util.UUID;

import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import com.hardcoded.error.TileException;
import com.hardcoded.lwjgl.WorldContentHandler;
import com.hardcoded.lwjgl.shader.AssetShader;
import com.hardcoded.lwjgl.shadow.ShadowShader;
import com.hardcoded.math.Quat;
import com.hardcoded.math.Vec3;
import com.hardcoded.prefab.readers.PrefabFileReader;
import com.hardcoded.tile.object.Asset;
import com.hardcoded.tile.object.Harvestable;
import com.hardcoded.tile.object.Prefab;

/**
 * A prefab renderer.
 * 
 * @author HardCoded
 * @since v0.2
 */
public class WorldPrefabRender {
	private WorldContentHandler handler;
	
	private AssetShader assetShader;
	
	// DEBUG
	public String path;
	public Prefab prefab;
	
	public WorldPrefabRender(WorldContentHandler handler, String path) {
		this.handler = handler;
		this.assetShader = handler.assetShader;
		this.path = path;
		
		try {
			prefab = PrefabFileReader.readPrefab(handler.getContext().resolve(path));
		} catch(TileException | IOException e) {
			e.printStackTrace();
		}
	}
	
	public void render(Matrix4f transform, Matrix4f projectionView, Matrix4f viewMatrix, Matrix4f toShadowSpace) {
		if(true) return;
		
		assetShader.bind();
		assetShader.setProjectionView(projectionView);
		assetShader.setViewMatrix(viewMatrix);
		assetShader.setModelMatrix(transform);
		assetShader.setShadowMapSpace(toShadowSpace);
		
		for(Asset asset : prefab.getAssets()) {
			UUID uuid = asset.getUuid();
			Vec3 apos = asset.getPosition();
			Quat arot = asset.getRotation();
			Vec3 _size = asset.getSize();
			
			WorldAssetRender mesh = handler.getAssetRender(uuid);
			if(mesh != null) {
				Matrix4f modelMatrix = new Matrix4f(transform)
					.translate(new Vector3f(apos.toArray()))
					.rotate(new Quaternionf(arot.getX(), arot.getY(), arot.getZ(), arot.getW()))
					.scale(_size.getX(), _size.getY(), _size.getZ());
				
				assetShader.setModelMatrix(modelMatrix);
				mesh.render(asset);
			}
		}
		
		for(Harvestable harvestable : prefab.getHarvestables()) {
			UUID uuid = harvestable.getUuid();
			Vec3 apos = harvestable.getPosition();
			Quat arot = harvestable.getRotation();
			Vec3 _size = harvestable.getSize();
			
			WorldHarvestableRender mesh = handler.getHarvestableRender(uuid);
			if(mesh != null) {
				Matrix4f modelMatrix = new Matrix4f(transform)
					.translate(new Vector3f(apos.toArray()))
					.rotate(new Quaternionf(arot.getX(), arot.getY(), arot.getZ(), arot.getW()))
					.scale(_size.getX(), _size.getY(), _size.getZ());
				
				assetShader.setModelMatrix(modelMatrix);
				mesh.render(harvestable);
			}
		}
		
		assetShader.unbind();
		
		for(Prefab prefab : prefab.getPrefabs()) {
			Vec3 apos = prefab.getPosition();
			Quat arot = prefab.getRotation();
			Vec3 _size = prefab.getSize();
			
			WorldPrefabRender render = handler.getPrefabRender(prefab.getPath());
			if(render != null) {
				Matrix4f modelMatrix = new Matrix4f(transform)
					.translate(new Vector3f(apos.toArray()))
					.rotate(new Quaternionf(arot.getX(), arot.getY(), arot.getZ(), arot.getW()))
					.scale(_size.getX(), _size.getY(), _size.getZ());
				
				render.render(modelMatrix, projectionView, viewMatrix, toShadowSpace);
			}
		}
		
		// TODO: Blueprints / Nodes
	}
	
	public void renderShadows(ShadowShader shader, Matrix4f mvpMatrix) {
		for(Asset asset : prefab.getAssets()) {
			UUID uuid = asset.getUuid();
			Vec3 apos = asset.getPosition();
			Quat arot = asset.getRotation();
			Vec3 _size = asset.getSize();
			
			WorldAssetRender mesh = handler.getAssetRender(uuid);
			if(mesh != null) {
				Matrix4f modelMatrix = new Matrix4f(mvpMatrix)
					.translate(new Vector3f(apos.toArray()))
					.rotate(new Quaternionf(arot.getX(), arot.getY(), arot.getZ(), arot.getW()))
					.scale(_size.getX(), _size.getY(), _size.getZ());
				
				shader.setMvpMatrix(modelMatrix);
				mesh.render(asset);
			}
		}
		
		for(Harvestable harvestable : prefab.getHarvestables()) {
			UUID uuid = harvestable.getUuid();
			Vec3 apos = harvestable.getPosition();
			Quat arot = harvestable.getRotation();
			Vec3 _size = harvestable.getSize();
			
			WorldHarvestableRender mesh = handler.getHarvestableRender(uuid);
			if(mesh != null) {
				Matrix4f modelMatrix = new Matrix4f(mvpMatrix)
					.translate(new Vector3f(apos.toArray()))
					.rotate(new Quaternionf(arot.getX(), arot.getY(), arot.getZ(), arot.getW()))
					.scale(_size.getX(), _size.getY(), _size.getZ());
				
				shader.setMvpMatrix(modelMatrix);
				mesh.render(harvestable);
			}
		}
		
		for(Prefab prefab : prefab.getPrefabs()) {
			Vec3 apos = prefab.getPosition();
			Quat arot = prefab.getRotation();
			Vec3 _size = prefab.getSize();
			
			WorldPrefabRender render = handler.getPrefabRender(prefab.getPath());
			if(render != null) {
				Matrix4f modelMatrix = new Matrix4f(mvpMatrix)
					.translate(new Vector3f(apos.toArray()))
					.rotate(new Quaternionf(arot.getX(), arot.getY(), arot.getZ(), arot.getW()))
					.scale(_size.getX(), _size.getY(), _size.getZ());
			
				render.renderShadows(shader, modelMatrix);
			}
		}
		
		// TODO: Blueprints / Nodes
	}
}
