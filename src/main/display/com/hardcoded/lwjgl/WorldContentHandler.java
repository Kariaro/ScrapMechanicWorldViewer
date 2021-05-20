package com.hardcoded.lwjgl;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import com.hardcoded.asset.ScrapMechanicAssetHandler;
import com.hardcoded.db.types.*;
import com.hardcoded.error.TileException;
import com.hardcoded.game.GameContext;
import com.hardcoded.logger.Log;
import com.hardcoded.lwjgl.cache.*;
import com.hardcoded.lwjgl.data.TileParts;
import com.hardcoded.lwjgl.shader.*;
import com.hardcoded.lwjgl.shadow.ShadowFrameBuffer;
import com.hardcoded.lwjgl.shadow.ShadowShader;
import com.hardcoded.prefab.readers.PrefabFileReader;
import com.hardcoded.sm.objects.TileData;
import com.hardcoded.tile.Tile;
import com.hardcoded.tile.TileReader;
import com.hardcoded.tile.object.Blueprint;
import com.hardcoded.tile.object.Prefab;

/**
 * This class contains all the asset types for the game
 * 
 * @author HardCoded
 * @since v0.2
 */
public class WorldContentHandler {
	private static final Log LOGGER = Log.getLogger();
	
	private final Map<UUID, WorldHarvestableCache> harvestables;
	private final Map<UUID, WorldAssetCache> assets;
	private final Map<UUID, WorldBlockCache> blocks;
	private final Map<UUID, WorldPartCache> parts;
	private final Map<Long, WorldTileCache> tiles;
	private final Map<String, TileParts> tile_data;
	
	private final Map<String, WorldBlueprintCache> blueprints;
	private final Map<String, Prefab> prefabs;
	
	public BlockShader blockShader;
	public AssetShader assetShader;
	public PartShader partShader;
	public TileShader tileShader;
	public ShadowShader shadowShader;
	public ShadowFrameBuffer frameBuffer;
	
	private GameContext context;
	private int load_limit;
	
	protected WorldContentHandler() {
		harvestables = new HashMap<>();
		blueprints = new HashMap<>();
		prefabs = new HashMap<>();
		assets = new HashMap<>();
		blocks = new HashMap<>();
		parts = new HashMap<>();
		tiles = new HashMap<>();
		tile_data = new HashMap<>();
	}
	
	protected void init() {
		blockShader = new BlockShader();
		assetShader = new AssetShader();
		partShader = new PartShader();
		tileShader = new TileShader();
		
		shadowShader = new ShadowShader();
		frameBuffer = new ShadowFrameBuffer(2048, 2048);
		
		context = new GameContext(ScrapMechanicAssetHandler.getGamePath());
	}
	
	public GameContext getContext() {
		return context;
	}
	
	protected void setLoadLimit(int limit) {
		this.load_limit = limit;
	}
	
	private boolean loadCheck() {
		if(load_limit == -1) return true;
		if(load_limit > 0) {
			load_limit--;
			return true;
		}
		
		return false;
	}
	
	public WorldBlockCache getBlockCache(UUID uuid) {
		WorldBlockCache cache = blocks.get(uuid);
		if(cache != null) return cache;
		
		SMBlock block = ScrapMechanicAssetHandler.getBlock(uuid);
		if(block == null || !loadCheck()) return null;
		
		cache = new WorldBlockCache(block);
		blocks.put(block.uuid, cache);
		return cache;
	}
	
	public WorldPartCache getPartCache(UUID uuid) {
		WorldPartCache cache = parts.get(uuid);
		if(cache != null) return cache;
		
		SMPart part = ScrapMechanicAssetHandler.getPart(uuid);
		if(part == null || !loadCheck()) return null;
		
		LOGGER.info("Init: %s", part);
		cache = new WorldPartCache(part, partShader);
		parts.put(part.uuid, cache);
		return cache;
	}
	
	public WorldAssetCache getAssetCache(UUID uuid) {
		WorldAssetCache cache = assets.get(uuid);
		if(cache != null) return cache;
		
		SMAsset asset = ScrapMechanicAssetHandler.getAsset(uuid);
		if(asset == null || !loadCheck()) return null;
		
		LOGGER.info("Init: %s", asset);
		cache = new WorldAssetCache(asset, assetShader);
		assets.put(asset.uuid, cache);
		return cache;
	}
	
	public WorldHarvestableCache getHarvestableCache(UUID uuid) {
		WorldHarvestableCache cache = harvestables.get(uuid);
		if(cache != null) return cache;
		
		SMHarvestable harvestable = ScrapMechanicAssetHandler.getHarvestable(uuid);
		if(harvestable == null || !loadCheck()) return null;
		
		LOGGER.info("Init: %s", harvestable);
		cache = new WorldHarvestableCache(harvestable, assetShader);
		harvestables.put(harvestable.uuid, cache);
		return cache;
	}
	
	public Prefab getPrefabCache(String path) {
		Prefab cache = prefabs.get(path);
		if(cache != null) return cache;
		
		if(!loadCheck()) return null;
		
		try {
			cache = PrefabFileReader.readPrefab(context.resolve(path));
			LOGGER.info("Init: %s", path);
			prefabs.put(path, cache);
			return cache;
		} catch(TileException | IOException e) {
			LOGGER.throwing(e);
			return null;
		}
	}
	
	public WorldBlueprintCache getBlueprintCache(Blueprint blueprint) {
		String value = blueprint.getValue();
		WorldBlueprintCache cache = blueprints.get(value);
		if(cache != null) return cache;
		
		if(!loadCheck()) return null;
		
		cache = new WorldBlueprintCache(this, blueprint);
		LOGGER.info("Init: %s", cache);
		blueprints.put(value, cache);
		return cache;
	}
	
	public WorldTileCache getTileCache(int x, int y) {
		if(!TileData.hasTile(x, y)) return null;
		
		long index = TileData.getTileId(x, y);
		if(tiles.containsKey(index)) return tiles.get(index);

		String path = TileData.getTilePath(x, y);
		if(path == null || !loadCheck()) return null;
		
		TileParts parts = null;
		if(tile_data.containsKey(path)) {
			parts = tile_data.get(path);
		} else {
			try {
				Tile tile = TileReader.readTile(path, context);
				parts = new TileParts(tile);
				tile_data.put(path, parts);
			} catch(TileException e) {
				LOGGER.throwing(e);
			} catch(IOException e) {
				LOGGER.throwing(e);
			}
		}
		
		LOGGER.info("Init: '%s'", path);
		WorldTileCache render = new WorldTileCache(this, x, y, parts);
		tiles.put(index, render);
		return render;
	}
}
