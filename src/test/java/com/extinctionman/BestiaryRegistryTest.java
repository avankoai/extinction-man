package com.extinctionman;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class BestiaryRegistryTest
{
	@Test
	public void onlyAcceptsNamedNpcsWithAttackAction()
	{
		assertTrue(BestiaryRegistry.isAttackable("Goblin", new String[]{"Talk-to", "Attack", "Examine"}, 2));
		assertTrue(BestiaryRegistry.isAttackable("Rat", new String[]{"Attack"}, 1));
		assertFalse(BestiaryRegistry.isAttackable("Banker", new String[]{"Bank", "Talk-to"}, 0));
		assertFalse(BestiaryRegistry.isAttackable("Barricade", new String[]{"Attack"}, 0));
		assertFalse(BestiaryRegistry.isAttackable("Core", new String[]{"Attack"}, -1));
		assertFalse(BestiaryRegistry.isAttackable("null", new String[]{"Attack"}, 5));
		assertFalse(BestiaryRegistry.isAttackable(null, new String[]{"Attack"}, 5));
	}

	@Test
	public void includesCombinedRaidActivitiesAndCanExcludeSpecialNpcs()
	{
		BestiaryRegistry registry = new BestiaryRegistry();
		assertTrue(registry.getAttackableNames().contains(SpecialEncounterRules.CHAMBERS_OF_XERIC));
		assertTrue(registry.getAttackableNames().contains(SpecialEncounterRules.THEATRE_OF_BLOOD));
		assertTrue(registry.getAttackableNames().contains(SpecialEncounterRules.TOMBS_OF_AMASCUT));
		assertTrue(registry.excludeSpecialNpc("Ba-Ba"));
		assertTrue(registry.isExcludedSpecialNpc("Ba-Ba"));
		assertFalse(registry.excludeSpecialNpc(SpecialEncounterRules.CHAMBERS_OF_XERIC));
	}
}
