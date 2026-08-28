package com.extinctionman;

import java.util.ArrayList;
import java.util.List;
import javax.inject.Singleton;
import net.runelite.api.coords.WorldPoint;

@Singleton
final class WhitelistLootTracker
{
	static final int LIFETIME_TICKS = 300;
	private final List<WhitelistLootMarker> markers = new ArrayList<>();

	synchronized void add(WorldPoint worldPoint, int itemId, String itemName, int currentTick)
	{
		if (worldPoint == null) return;
		markers.removeIf(marker -> marker.getWorldPoint().equals(worldPoint)
			&& marker.getItemId() == itemId);
		markers.add(new WhitelistLootMarker(worldPoint, itemId, itemName,
			currentTick + LIFETIME_TICKS));
	}

	synchronized List<WhitelistLootMarker> active(int currentTick)
	{
		markers.removeIf(marker -> marker.isExpired(currentTick));
		return new ArrayList<>(markers);
	}

	synchronized void clear()
	{
		markers.clear();
	}
}
