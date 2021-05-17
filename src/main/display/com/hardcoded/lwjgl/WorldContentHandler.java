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
import com.hardcoded.lwjgl.render.*;
import com.hardcoded.lwjgl.shader.*;
import com.hardcoded.lwjgl.shadow.ShadowFrameBuffer;
import com.hardcoded.lwjgl.shadow.ShadowShader;
import com.hardcoded.sm.objects.TileData;
import com.hardcoded.tile.Tile;
import com.hardcoded.tile.TileReader;

/**
 * This class contains all the asset types for the game
 * 
 * @author HardCoded
 * @since v0.2
 */
public class WorldContentHandler {
	private static final Log LOGGER = Log.getLogger();
	
	private final Map<UUID, WorldHarvestableRender> harvestables;
	private final Map<UUID, WorldAssetRender> assets;
	private final Map<UUID, WorldBlockRender> blocks;
	private final Map<UUID, WorldPartRender> parts;
	private final Map<Long, WorldTileRender> tiles;
	private final Map<String, TileParts> tile_data;
	
	protected BlockShader blockShader;
	protected AssetShader assetShader;
	protected PartShader partShader;
	protected TileShader tileShader;
	protected ShadowShader shadowShader;
	protected ShadowFrameBuffer frameBuffer;
	
	private GameContext context;
	private int load_limit;
	
	protected WorldContentHandler() {
		harvestables = new HashMap<>();
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
	
	protected void setLoadLimit(int limit) {
		this.load_limit = limit;
	}
	
	private boolean loadCheck() {
		return load_limit-- > 0;
	}
	
	public WorldBlockRender getBlockRender(UUID uuid) {
		if(blocks.containsKey(uuid)) return blocks.get(uuid);
		
		SMBlock block = ScrapMechanicAssetHandler.getBlock(uuid);
		if(block == null || !loadCheck()) return null;
		
		WorldBlockRender render = new WorldBlockRender(block, blockShader);
		blocks.put(block.uuid, render);
		return render;
	}
	
	public WorldPartRender getPartRender(UUID uuid) {
		if(parts.containsKey(uuid)) return parts.get(uuid);
		
		SMPart part = ScrapMechanicAssetHandler.getPart(uuid);
		if(part == null || !loadCheck()) return null;
		
		LOGGER.info("Init: %s", part);
		WorldPartRender render = new WorldPartRender(part, partShader);
		parts.put(part.uuid, render);
		return render;
	}
	
	public WorldAssetRender getAssetRender(UUID uuid) {
		if(assets.containsKey(uuid)) return assets.get(uuid);
		
		SMAsset asset = ScrapMechanicAssetHandler.getAsset(uuid);
		if(asset == null || !loadCheck()) return null;
		
		LOGGER.info("Init: %s", asset);
		WorldAssetRender render = new WorldAssetRender(asset, assetShader);
		assets.put(asset.uuid, render);
		return render;
	}
	
	public WorldHarvestableRender getHarvestableRender(UUID uuid) {
		if(harvestables.containsKey(uuid)) return harvestables.get(uuid);
		
		SMHarvestable harvestable = ScrapMechanicAssetHandler.getHarvestable(uuid);
		if(harvestable == null || !loadCheck()) return null;
		
		LOGGER.info("Init: %s", harvestable);
		WorldHarvestableRender render = new WorldHarvestableRender(harvestable, assetShader);
		harvestables.put(harvestable.uuid, render);
		return render;
	}
	
	public WorldTileRender getTileRender(int x, int y) {
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
		WorldTileRender render = new WorldTileRender(this, x, y, parts, tileShader, assetShader);
		tiles.put(index, render);
		return render;
	}
}
