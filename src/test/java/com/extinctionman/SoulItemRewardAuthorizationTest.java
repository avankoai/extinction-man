package com.extinctionman;

import org.junit.Test;
import static org.junit.Assert.*;

public class SoulItemRewardAuthorizationTest
{
	@Test
	public void preservesVerifiedSourceForMatchingImmediateReward()
	{
		SoulItemRewardAuthorization authorization = new SoulItemRewardAuthorization();
		authorization.authorize(100, "Corrupted Hunllef", 20);
		assertTrue(authorization.matches(100, 20));
		assertFalse(authorization.matches(200, 20));
		assertEquals("Corrupted Hunllef", authorization.source(100, 25));
		assertEquals("Corrupted Hunllef", authorization.consume(100, 25));
		assertNull(authorization.consume(100, 25));
	}

	@Test
	public void expiresWithoutAcceptingLaterInventoryChanges()
	{
		SoulItemRewardAuthorization authorization = new SoulItemRewardAuthorization();
		authorization.authorize(100, "Crystalline Hunllef", 20);
		assertNull(authorization.consume(100,
			20 + SoulItemRewardAuthorization.LIFETIME_TICKS + 1));
	}
}
