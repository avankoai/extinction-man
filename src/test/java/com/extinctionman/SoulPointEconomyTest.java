package com.extinctionman;

import java.util.Arrays;
import java.util.Collections;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class SoulPointEconomyTest
{
	@Test
	public void monstersAwardOneEnergyAndBossesAwardTen()
	{
		assertEquals(0, SoulPointEconomy.earnedEnergy(Collections.emptyList()));
		assertEquals(1, SoulPointEconomy.earnedEnergy(Collections.singletonList("Goblin")));
		assertEquals(10, SoulPointEconomy.earnedEnergy(Collections.singletonList("Vorkath")));
		assertEquals(11, SoulPointEconomy.earnedEnergy(Arrays.asList("Goblin", "Vorkath")));
	}

	@Test
	public void spendingUsesTheNewPointScale()
	{
		assertEquals(11, SoulPointEconomy.availableEnergy(Arrays.asList("Goblin", "Vorkath"), 0));
		assertEquals(1, SoulPointEconomy.availableEnergy(Arrays.asList("Goblin", "Vorkath"), 10));
		assertEquals(0, SoulPointEconomy.availableEnergy(Collections.singletonList("Goblin"), 10));
	}
}
