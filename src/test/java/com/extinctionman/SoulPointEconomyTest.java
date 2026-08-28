package com.extinctionman;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class SoulPointEconomyTest
{
	@Test
	public void banksOnePointAndResetsMeterEveryHundredExterminations()
	{
		assertEquals(0, SoulPointEconomy.earnedPoints(99));
		assertEquals(99, SoulPointEconomy.progressToNextPoint(99));
		assertEquals(1, SoulPointEconomy.earnedPoints(100));
		assertEquals(0, SoulPointEconomy.progressToNextPoint(100));
		assertEquals(2, SoulPointEconomy.earnedPoints(200));
		assertEquals(0, SoulPointEconomy.progressToNextPoint(200));
	}

	@Test
	public void spendingAnUnlockConsumesOneBankedPointOnly()
	{
		assertEquals(2, SoulPointEconomy.availablePoints(250, 0));
		assertEquals(1, SoulPointEconomy.availablePoints(250, 1));
		assertEquals(50, SoulPointEconomy.progressToNextPoint(250));
	}
}
