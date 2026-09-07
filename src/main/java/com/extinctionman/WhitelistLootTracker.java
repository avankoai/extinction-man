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
		add(worldPoint, itemId, itemName, null, currentTick);
	}

	synchronized void add(WorldPoint worldPoint, int itemId, String itemName, String sourceName,
		int currentTick)
	{
		if (worldPoint == null) return;
		markers.removeIf(marker -> marker.getWorldPoint().equals(worldPoint)
			&& marker.getItemId() == itemId);
		markers.add(new WhitelistLootMarker(worldPoint, itemId, itemName, sourceName,
			currentTick + LIFETIME_TICKS));
	}

	synchronized List<WhitelistLootMarker> active(int currentTick)
	{
		markers.removeIf(marker -> marker.isExpired(currentTick));
		return new ArrayList<>(markers);
	}

	synchronized boolean contains(WorldPoint worldPoint, int itemId, int currentTick)
	{
		return active(currentTick).stream().anyMatch(marker -> marker.getWorldPoint().equals(worldPoint)
			&& marker.getItemId() == itemId);
	}

	synchronized String sourceAt(WorldPoint worldPoint, int itemId, int currentTick)
	{
		for (WhitelistLootMarker marker : active(currentTick))
		{
			if (marker.getWorldPoint().equals(worldPoint) && marker.getItemId() == itemId)
				return marker.getSourceName();
		}
		return null;
	}

	synchronized void remove(WorldPoint worldPoint, int itemId)
	{
		if (worldPoint == null) return;
		markers.removeIf(marker -> marker.getWorldPoint().equals(worldPoint)
			&& marker.getItemId() == itemId);
	}

	synchronized List<WhitelistLootMarker> drain(int currentTick)
	{
		List<WhitelistLootMarker> remaining = active(currentTick);
		markers.clear();
		return remaining;
	}

	synchronized List<WhitelistLootMarker> drainMatching(String sourceName, int itemId,
		int currentTick)
	{
		List<WhitelistLootMarker> matches = new ArrayList<>();
		for (WhitelistLootMarker marker : active(currentTick))
		{
			if (marker.getItemId() == itemId && java.util.Objects.equals(sourceName,
				marker.getSourceName())) matches.add(marker);
		}
		markers.removeAll(matches);
		return matches;
	}

	synchronized void clear()
	{
		markers.clear();
	}
}
