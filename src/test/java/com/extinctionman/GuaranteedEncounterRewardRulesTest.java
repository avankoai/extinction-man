package com.extinctionman;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class GuaranteedEncounterRewardRulesTest
{
	@Test
	public void recognisesGuaranteedWaveCompletionItems()
	{
		assertTrue(GuaranteedEncounterRewardRules.matches("TzTok-Jad", "Fire cape"));
		assertTrue(GuaranteedEncounterRewardRules.matches("TzKal-Zuk", "Infernal cape"));
		assertFalse(GuaranteedEncounterRewardRules.matches("TzTok-Jad", "Infernal cape"));
		assertFalse(GuaranteedEncounterRewardRules.matches("Goblin", "Fire cape"));
	}
}
