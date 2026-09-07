package com.extinctionman;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class SoulPermitTest
{
	@Test
	public void normalAndBossPermitsHaveExactNamesAndDifferentCosts()
	{
		SoulPermit monster = new SoulPermit("Goblin", false);
		SoulPermit boss = new SoulPermit("Vorkath", true);
		assertEquals(1, monster.getPointCost());
		assertEquals(10, boss.getPointCost());
		assertTrue(monster.matches("goblin"));
		assertFalse(monster.matches("Goblin guard"));
	}

	@Test
	public void permitsCanHoldAndConsumeMultipleKills()
	{
		SoulPermit permit = new SoulPermit("Goblin", false, 3);
		assertEquals(3, permit.getRemainingKills());
		assertEquals(3, permit.getPointCost());
		assertEquals(2, permit.consumeOne().getRemainingKills());
		assertEquals(5, permit.addKills(2).getRemainingKills());
	}
}
