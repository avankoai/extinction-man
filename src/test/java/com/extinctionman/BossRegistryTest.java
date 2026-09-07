package com.extinctionman;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class BossRegistryTest
{
	@Test
	public void classifiesHiscoreBossSourcesWithoutTreatingNormalMonstersAsBosses()
	{
		assertTrue(BossRegistry.isBoss("Corporeal Beast"));
		assertTrue(BossRegistry.isBoss("Chambers of Xeric"));
		assertTrue(BossRegistry.isBoss("Dharok the Wretched"));
		assertFalse(BossRegistry.isBoss("Goblin"));
	}
}
