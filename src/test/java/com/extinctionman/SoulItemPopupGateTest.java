package com.extinctionman;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class SoulItemPopupGateTest
{
	@Test
	public void onlyClaimsOnceForTheSameHuntItem()
	{
		SoulItemPopupGate gate = new SoulItemPopupGate();
		assertTrue(gate.claim(123));
		assertFalse(gate.claim(123));
	}

	@Test
	public void resetsForANewHuntOrAfterClear()
	{
		SoulItemPopupGate gate = new SoulItemPopupGate();
		assertTrue(gate.claim(123));
		assertTrue(gate.claim(456));
		gate.clear();
		assertTrue(gate.claim(456));
	}
}
