package com.extinctionman;

import org.junit.Test;
import static org.junit.Assert.*;

public class SoulItemPickupAuthorizationTest
{
	@Test
	public void onlyMatchingRecentMarkedPickupIsAccepted()
	{
		SoulItemPickupAuthorization authorization = new SoulItemPickupAuthorization();
		authorization.authorize(100, 10);
		assertFalse(authorization.consume(200, 10));
		authorization.authorize(100, 10);
		assertTrue(authorization.consume(100, 12));
		assertFalse(authorization.consume(100, 12));
		authorization.authorize(100, 10);
		assertFalse(authorization.consume(100,
			10 + SoulItemPickupAuthorization.LIFETIME_TICKS + 1));
	}

	@Test
	public void preservesTheSourceOfTheSelectedGroundDrop()
	{
		SoulItemPickupAuthorization authorization = new SoulItemPickupAuthorization();
		authorization.authorize(100, "Goblin", 10);
		assertEquals("Goblin", authorization.consumeSource(100, 12));
		assertNull(authorization.consumeSource(100, 12));
	}
}
