package com.extinctionman;

import net.runelite.api.coords.WorldPoint;

final class WhitelistLootMarker
{
	private final WorldPoint worldPoint;
	private final int itemId;
	private final String itemName;
	private final int expiresAtTick;

	WhitelistLootMarker(WorldPoint worldPoint, int itemId, String itemName, int expiresAtTick)
	{
		this.worldPoint = worldPoint;
		this.itemId = itemId;
		this.itemName = itemName;
		this.expiresAtTick = expiresAtTick;
	}

	WorldPoint getWorldPoint() { return worldPoint; }
	int getItemId() { return itemId; }
	String getItemName() { return itemName; }
	boolean isExpired(int currentTick) { return currentTick >= expiresAtTick; }
}
