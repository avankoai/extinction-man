package com.extinctionman;

import java.util.ArrayList;
import java.util.List;
import javax.inject.Singleton;
import net.runelite.api.coords.WorldPoint;

@Singleton
final class ForbiddenLootTracker
{
	static final int LIFETIME_TICKS = 200;
	private final List<ForbiddenLootMarker> markers = new ArrayList<>();

	void add(WorldPoint worldPoint, int itemId, String itemName, int currentTick)
	{
		if (worldPoint == null)
		{
			return;
		}
		for (ForbiddenLootMarker marker : markers)
		{
			if (marker.getWorldPoint().equals(worldPoint))
			{
				marker.refresh(itemId, itemName, currentTick + LIFETIME_TICKS);
				return;
			}
		}
		markers.add(new ForbiddenLootMarker(worldPoint, itemId, itemName,
			currentTick + LIFETIME_TICKS));
	}

	boolean isForbidden(WorldPoint worldPoint, int itemId, int currentTick)
	{
		return active(currentTick).stream()
			.anyMatch(marker -> marker.getWorldPoint().equals(worldPoint) && marker.containsItem(itemId));
	}

	void remove(WorldPoint worldPoint, int itemId)
	{
		if (worldPoint == null) return;
		for (ForbiddenLootMarker marker : markers)
		{
			if (marker.getWorldPoint().equals(worldPoint)) marker.removeItem(itemId);
		}
		markers.removeIf(ForbiddenLootMarker::isEmpty);
	}

	List<ForbiddenLootMarker> active(int currentTick)
	{
		markers.removeIf(marker -> marker.isExpired(currentTick));
		return new ArrayList<>(markers);
	}

	void clear()
	{
		markers.clear();
	}
}
