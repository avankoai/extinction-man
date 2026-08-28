package com.extinctionman;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class SpecialEncounterRulesTest
{
	@Test
	public void mapsRaidRegionsToCombinedActivities()
	{
		assertEquals(SpecialEncounterRules.CHAMBERS_OF_XERIC,
			SpecialEncounterRules.raidAtRegion(13136));
		assertEquals(SpecialEncounterRules.THEATRE_OF_BLOOD,
			SpecialEncounterRules.raidAtRegion(12867));
		assertEquals(SpecialEncounterRules.TOMBS_OF_AMASCUT,
			SpecialEncounterRules.raidAtRegion(14160));
		assertNull(SpecialEncounterRules.raidAtRegion(12850));
	}

	@Test
	public void ignoresEveryNpcKillInsideRaids()
	{
		assertTrue(SpecialEncounterRules.shouldIgnoreNpcKill(14160, "Ba-Ba"));
		assertTrue(SpecialEncounterRules.shouldIgnoreNpcKill(12867, "Verzik Vitur"));
		assertTrue(SpecialEncounterRules.shouldIgnoreNpcKill(13136, "Great Olm"));
	}

	@Test
	public void onlyFinalBossCountsInWaveActivities()
	{
		assertTrue(SpecialEncounterRules.shouldIgnoreNpcKill(
			SpecialEncounterRules.FIGHT_CAVES_REGION, "Ket-Zek"));
		assertFalse(SpecialEncounterRules.shouldIgnoreNpcKill(
			SpecialEncounterRules.FIGHT_CAVES_REGION, SpecialEncounterRules.TZTOK_JAD));
		assertTrue(SpecialEncounterRules.shouldIgnoreNpcKill(
			SpecialEncounterRules.INFERNO_REGION, "JalTok-Jad"));
		assertFalse(SpecialEncounterRules.shouldIgnoreNpcKill(
			SpecialEncounterRules.INFERNO_REGION, SpecialEncounterRules.TZKAL_ZUK));
	}

	@Test
	public void canonicalizesRaidBossesButNotTrashMobs()
	{
		assertEquals("Ba-Ba", SpecialEncounterRules.canonicalRaidBoss(
			SpecialEncounterRules.TOMBS_OF_AMASCUT, "Ba-Ba"));
		assertEquals("Verzik Vitur", SpecialEncounterRules.canonicalRaidBoss(
			SpecialEncounterRules.THEATRE_OF_BLOOD, "Verzik Vitur"));
		assertEquals("Great Olm", SpecialEncounterRules.canonicalRaidBoss(
			SpecialEncounterRules.CHAMBERS_OF_XERIC, "Great Olm"));
		assertEquals("Vanguard", SpecialEncounterRules.canonicalRaidBoss(
			SpecialEncounterRules.CHAMBERS_OF_XERIC, "Vanguard"));
		assertNull(SpecialEncounterRules.canonicalRaidBoss(
			SpecialEncounterRules.TOMBS_OF_AMASCUT, "Baboon Thrower"));
	}

	@Test
	public void handlesWaveAndInstanceActivitiesWithoutAffectingPestControl()
	{
		assertTrue(SpecialEncounterRules.shouldIgnoreNpcKill(
			SpecialEncounterRules.FORTIS_COLOSSEUM_REGION, "Fremennik warband archer"));
		assertFalse(SpecialEncounterRules.shouldIgnoreNpcKill(
			SpecialEncounterRules.FORTIS_COLOSSEUM_REGION, "Sol Heredit"));
		assertTrue(SpecialEncounterRules.shouldIgnoreNpcKill(
			SpecialEncounterRules.GAUNTLET_REGION, "Crystalline dragon"));
		assertFalse(SpecialEncounterRules.shouldIgnoreNpcKill(
			SpecialEncounterRules.GAUNTLET_REGION, "Crystalline Hunllef"));
		assertTrue(SpecialEncounterRules.shouldIgnoreNpcKill(
			SpecialEncounterRules.CORRUPTED_GAUNTLET_REGION, "Corrupted dragon"));
		assertFalse(SpecialEncounterRules.shouldIgnoreNpcKill(
			SpecialEncounterRules.CORRUPTED_GAUNTLET_REGION, "Corrupted Hunllef"));
		assertFalse(SpecialEncounterRules.shouldIgnoreNpcKill(
			SpecialEncounterRules.NIGHTMARE_ZONE_REGION, "Count Draynor"));
		assertTrue(SpecialEncounterRules.shouldIgnoreNpcKill(8493, "Avatar of Creation"));
		assertTrue(SpecialEncounterRules.shouldIgnoreNpcKill(7508, "Penance Fighter"));
		assertTrue(SpecialEncounterRules.shouldIgnoreNpcKill(10322, "Penance Fighter"));
		assertFalse(SpecialEncounterRules.shouldIgnoreNpcKill(7508, "Penance Queen"));
		assertFalse(SpecialEncounterRules.shouldIgnoreNpcKill(10537, "Brawler"));
	}

	@Test
	public void keepsGwdSupportAvailableButNotTheGenerals()
	{
		assertTrue(SpecialEncounterRules.isGodWarsSupportNpc(
			11603, "Goblin"));
		assertTrue(SpecialEncounterRules.isGodWarsSupportNpc(
			11347, "Sergeant Steelwill"));
		assertFalse(SpecialEncounterRules.isGodWarsSupportNpc(
			11347, "General Graardor"));
		assertFalse(SpecialEncounterRules.isGodWarsSupportNpc(12345, "Goblin"));
	}

	@Test
	public void linksEachGauntletToItsOwnHunllef()
	{
		assertEquals("Crystalline Hunllef",
			SpecialEncounterRules.gauntletBossForRegion(SpecialEncounterRules.GAUNTLET_REGION));
		assertEquals("Corrupted Hunllef",
			SpecialEncounterRules.gauntletBossForRegion(SpecialEncounterRules.CORRUPTED_GAUNTLET_REGION));
		assertNull(SpecialEncounterRules.gauntletBossForRegion(12345));
	}

	@Test
	public void ignoresMandatoryTechnicalBossParts()
	{
		assertTrue(SpecialEncounterRules.alwaysIgnoredTechnicalNpc("Respiratory system"));
		assertTrue(SpecialEncounterRules.alwaysIgnoredTechnicalNpc("Enormous Tentacle"));
		assertTrue(SpecialEncounterRules.alwaysIgnoredTechnicalNpc("Abyssal portal"));
		assertTrue(SpecialEncounterRules.shouldAlwaysRemainVisible(12345, "Enormous Tentacle"));
		assertFalse(SpecialEncounterRules.alwaysIgnoredTechnicalNpc("Dusk"));
	}
}
