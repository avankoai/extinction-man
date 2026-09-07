package com.extinctionman;

import net.runelite.api.coords.WorldPoint;
import org.junit.Test;

import static org.junit.Assert.*;

public class WhitelistLootTrackerTest
{
	@Test
	public void whitelistBeamMarkerExpiresAndDoesNotDuplicate()
	{
		WhitelistLootTracker tracker = new WhitelistLootTracker();
		WorldPoint tile = new WorldPoint(3200, 3200, 0);
		tracker.add(tile, 526, "Bones", 10);
		tracker.add(tile, 526, "Bones", 11);
		assertEquals(1, tracker.active(11).size());
		assertEquals(0, tracker.active(11 + WhitelistLootTracker.LIFETIME_TICKS).size());
	}

	@Test
	public void markerCanBeMatchedRemovedAndDrained()
	{
		WhitelistLootTracker tracker = new WhitelistLootTracker();
		WorldPoint first = new WorldPoint(3200, 3200, 0);
		WorldPoint second = new WorldPoint(3201, 3200, 0);
		tracker.add(first, 100, "Hammer", 10);
		tracker.add(second, 100, "Hammer", 10);
		assertTrue(tracker.contains(first, 100, 10));
		tracker.remove(first, 100);
		assertFalse(tracker.contains(first, 100, 10));
		assertEquals(1, tracker.drain(10).size());
		assertEquals(0, tracker.active(10).size());
	}

	@Test
	public void markerRetainsItsUnboundSoulSource()
	{
		WhitelistLootTracker tracker = new WhitelistLootTracker();
		WorldPoint tile = new WorldPoint(3200, 3200, 0);
		tracker.add(tile, 100, "Hammer", "Goblin", 10);
		assertEquals("Goblin", tracker.sourceAt(tile, 100, 10));
		assertEquals(1, tracker.drainMatching("Goblin", 100, 10).size());
		assertEquals(0, tracker.active(10).size());
	}
}
