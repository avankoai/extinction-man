package com.extinctionman;

import net.runelite.api.coords.WorldPoint;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class ForbiddenLootTrackerTest
{
	@Test
	public void forbiddenGroundLabelUsesLockedSuffix()
	{
		assertEquals("Bones (Locked)", ForbiddenLootOverlay.lockedLabel("Bones"));
	}

	@Test
	public void markerExpiresAfterConfiguredLifetime()
	{
		ForbiddenLootTracker tracker = new ForbiddenLootTracker();
		tracker.add(new WorldPoint(3200, 3200, 0), 1001, "Hammer", 10);
		assertEquals(1, tracker.active(10).size());
		assertEquals(0, tracker.active(10 + ForbiddenLootTracker.LIFETIME_TICKS).size());
	}

	@Test
	public void sameTileRefreshesInsteadOfDuplicating()
	{
		ForbiddenLootTracker tracker = new ForbiddenLootTracker();
		WorldPoint tile = new WorldPoint(3200, 3200, 0);
		tracker.add(tile, 1001, "Hammer", 10);
		tracker.add(tile, 1002, "Bones", 20);
		assertEquals(1, tracker.active(20).size());
		assertEquals(2, tracker.active(20).get(0).getItemNames().size());
		assertTrue(tracker.isForbidden(tile, 1001, 20));
		assertTrue(tracker.isForbidden(tile, 1002, 20));
		assertFalse(tracker.isForbidden(tile, 1003, 20));
	}

	@Test
	public void despawnRemovesOnlyMatchingItemFromTile()
	{
		ForbiddenLootTracker tracker = new ForbiddenLootTracker();
		WorldPoint tile = new WorldPoint(3200, 3200, 0);
		tracker.add(tile, 1001, "Hammer", 10);
		tracker.add(tile, 1002, "Bones", 10);
		tracker.remove(tile, 1001);
		assertFalse(tracker.isForbidden(tile, 1001, 10));
		assertTrue(tracker.isForbidden(tile, 1002, 10));
		tracker.remove(tile, 1002);
		assertEquals(0, tracker.active(10).size());
	}
}
