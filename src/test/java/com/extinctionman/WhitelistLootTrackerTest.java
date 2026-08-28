package com.extinctionman;

import net.runelite.api.coords.WorldPoint;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

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
}
