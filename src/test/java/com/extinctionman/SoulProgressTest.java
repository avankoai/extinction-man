package com.extinctionman;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

public class SoulProgressTest
{
	@Test
	public void testKillCompletesFreshAndPartialProgressWithoutOverflow()
	{
		assertEquals(100, new SoulProgress("Goblin", 0).gainSouls(100).getSouls());
		assertEquals(100, new SoulProgress("Goblin", 43).gainSouls(100).getSouls());
		assertEquals(100, new SoulProgress("Goblin", 43).gainSouls(Integer.MAX_VALUE).getSouls());
		assertEquals(43, new SoulProgress("Goblin", 43).gainSouls(-1).getSouls());
	}

	@Test
	public void acceleratedKillsRequireDeveloperModeAndTestToggle()
	{
		assertEquals(100, ExtinctionManPlugin.killSoulAmount(true, true));
		assertEquals(1, ExtinctionManPlugin.killSoulAmount(true, false));
		assertEquals(1, ExtinctionManPlugin.killSoulAmount(false, true));
		assertEquals(1, ExtinctionManPlugin.killSoulAmount(false, false));
	}
	@Test
	public void gainsOneSoulAndPreservesExactName()
	{
		SoulProgress progress = new SoulProgress("Goblin (level-2)", 0).gainSoul();
		assertEquals("Goblin (level-2)", progress.getNpcName());
		assertEquals(1, progress.getSouls());
		assertFalse(progress.isExtinct());
	}

	@Test
	public void becomesExtinctAtOneHundred()
	{
		SoulProgress progress = new SoulProgress("Goblin", 99).gainSoul();
		assertEquals(100, progress.getSouls());
		assertTrue(progress.isExtinct());
	}

	@Test
	public void neverCountsPastOneHundred()
	{
		SoulProgress extinct = new SoulProgress("Goblin", 100);
		assertSame(extinct, extinct.gainSoul());
		assertEquals(100, extinct.gainSoul().getSouls());
	}
}
