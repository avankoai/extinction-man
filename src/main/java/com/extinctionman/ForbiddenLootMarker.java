package com.extinctionman;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import net.runelite.api.coords.WorldPoint;

final class ForbiddenLootMarker
{
	private final WorldPoint worldPoint;
	private final Map<Integer, String> items = new LinkedHashMap<>();
	private int expiresAtTick;

	ForbiddenLootMarker(WorldPoint worldPoint, int itemId, String itemName, int expiresAtTick)
	{
		this.worldPoint = worldPoint;
		this.items.put(itemId, itemName);
		this.expiresAtTick = expiresAtTick;
	}

	void refresh(int itemId, String itemName, int newExpiresAtTick)
	{
		items.put(itemId, itemName);
		expiresAtTick = newExpiresAtTick;
	}

	boolean containsItem(int itemId)
	{
		return items.containsKey(itemId);
	}

	void removeItem(int itemId)
	{
		items.remove(itemId);
	}

	boolean isEmpty()
	{
		return items.isEmpty();
	}

	List<String> getItemNames()
	{
		return new ArrayList<>(items.values());
	}

	WorldPoint getWorldPoint()
	{
		return worldPoint;
	}

	boolean isExpired(int currentTick)
	{
		return currentTick >= expiresAtTick;
	}
}
