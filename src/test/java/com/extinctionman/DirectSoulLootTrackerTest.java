package com.extinctionman;

import org.junit.Test;
import static org.junit.Assert.*;

public class DirectSoulLootTrackerTest
{
	@Test
	public void acceptsOnlyARealInventoryIncreaseForServerConfirmedLoot()
	{
		DirectSoulLootTracker tracker = new DirectSoulLootTracker();
		tracker.observe("Goblin", 100, 20);
		assertEquals("Goblin", tracker.claimInventoryIncrease(100, 20));
	}

	@Test
	public void matchingGroundSpawnPreventsDirectCompletion()
	{
		DirectSoulLootTracker tracker = new DirectSoulLootTracker();
		tracker.observe("Goblin", 100, 20);
		tracker.markGround("Goblin", 100, 20);
		assertNull(tracker.claimInventoryIncrease(100, 21));
	}

	@Test
	public void groundEventBeforeServerEventStillPreventsDirectCompletion()
	{
		DirectSoulLootTracker tracker = new DirectSoulLootTracker();
		tracker.markGround("Goblin", 100, 20);
		tracker.observe("Goblin", 100, 20);
		assertNull(tracker.claimInventoryIncrease(100, 21));
	}

	@Test
	public void adjacentTickGroundEventStillWinsOverDirectClassification()
	{
		DirectSoulLootTracker tracker = new DirectSoulLootTracker();
		tracker.markGround("Goblin", 100, 20);
		tracker.observe("Goblin", 100, 21);
		assertNull(tracker.claimInventoryIncrease(100, 22));
	}

	@Test
	public void rejectsWrongItemAndExpiredInventoryChanges()
	{
		DirectSoulLootTracker tracker = new DirectSoulLootTracker();
		tracker.observe("Goblin", 100, 20);
		assertNull(tracker.claimInventoryIncrease(101, 20));
		assertNull(tracker.claimInventoryIncrease(100, 23));
	}
}
