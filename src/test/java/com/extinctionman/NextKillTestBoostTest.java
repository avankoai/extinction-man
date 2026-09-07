package com.extinctionman;

import org.junit.Test;
import static org.junit.Assert.*;

public class NextKillTestBoostTest
{
	@Test
	public void appliesToExactlyOneEligibleKill()
	{
		NextKillTestBoost boost = new NextKillTestBoost();
		assertFalse(boost.consume());
		boost.arm();
		assertTrue(boost.isArmed());
		assertTrue(boost.consume());
		assertFalse(boost.consume());
		assertFalse(boost.isArmed());
	}
}
