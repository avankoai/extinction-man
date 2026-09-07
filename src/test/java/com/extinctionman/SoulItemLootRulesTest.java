package com.extinctionman;

import org.junit.Test;
import static org.junit.Assert.*;

public class SoulItemLootRulesTest
{
	@Test
	public void completedHuntDoesNotCreatePermanentLootPermission()
	{
		WhitelistUnlock completed = new WhitelistUnlock(526, "Bones", "Thief", true, 1);
		assertFalse(SoulItemLootRules.allowsActiveHuntItem(null, "Thief", 526, "Bones"));
		assertFalse(SoulItemLootRules.allowsActiveHuntItem(completed, "Thief", 526, "Bones"));
	}

	@Test
	public void onlyCurrentTargetFromCurrentSourceIsAllowed()
	{
		WhitelistUnlock active = new WhitelistUnlock(526, "Bones", "Thief", false, 1);
		assertTrue(SoulItemLootRules.allowsActiveHuntItem(active, "Thief", 526, "Bones"));
		assertFalse(SoulItemLootRules.allowsActiveHuntItem(active, "Thief", 313, "Fishing bait"));
		assertFalse(SoulItemLootRules.allowsActiveHuntItem(active, "Goblin", 526, "Bones"));
	}
}
