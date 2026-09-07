package com.extinctionman;

import net.runelite.api.coords.WorldPoint;

final class WhitelistLootMarker
{
	private final WorldPoint worldPoint;
	private final int itemId;
	private final String itemName;
	private final String sourceName;
	private final int expiresAtTick;

	WhitelistLootMarker(WorldPoint worldPoint, int itemId, String itemName, int expiresAtTick)
	{
		this(worldPoint, itemId, itemName, null, expiresAtTick);
	}

	WhitelistLootMarker(WorldPoint worldPoint, int itemId, String itemName, String sourceName,
		int expiresAtTick)
	{
		this.worldPoint = worldPoint;
		this.itemId = itemId;
		this.itemName = itemName;
		this.sourceName = sourceName;
		this.expiresAtTick = expiresAtTick;
	}

	WorldPoint getWorldPoint() { return worldPoint; }
	int getItemId() { return itemId; }
	String getItemName() { return itemName; }
	String getSourceName() { return sourceName; }
	boolean isExpired(int currentTick) { return currentTick >= expiresAtTick; }
}
