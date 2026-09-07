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
	public void raidExtinctionHidesAttackableRaidNpcsAsOneGroup()
	{
		assertTrue(SpecialEncounterRules.shouldHideRaidNpc(true, false,
			"Great Olm", new String[]{"Attack"}, 1043));
		assertTrue(SpecialEncounterRules.shouldHideRaidNpc(true, false,
			"Deathly ranger", new String[]{"Attack"}, 126));
		assertFalse(SpecialEncounterRules.shouldHideRaidNpc(false, false,
			"Great Olm", new String[]{"Attack"}, 1043));
		assertFalse(SpecialEncounterRules.shouldHideRaidNpc(true, true,
			"Great Olm", new String[]{"Attack"}, 1043));
		assertFalse(SpecialEncounterRules.shouldHideRaidNpc(true, false,
			"Captain Rimor", new String[]{"Talk-to"}, 0));
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
		assertNull(SpecialEncounterRules.canonicalRaidBoss(
			SpecialEncounterRules.CHAMBERS_OF_XERIC, "Left claw"));
		assertNull(SpecialEncounterRules.canonicalRaidBoss(
			SpecialEncounterRules.CHAMBERS_OF_XERIC, "Great Olm (right claw)"));
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
		assertTrue(SpecialEncounterRules.shouldIgnoreNpcKill(
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
	public void countsColosseumAndDoomAtRewardClaimInsteadOfEachFight()
	{
		assertTrue(SpecialEncounterRules.shouldIgnoreNpcKill(
			SpecialEncounterRules.FORTIS_COLOSSEUM_REGION, SpecialEncounterRules.SOL_HEREDIT));
		assertTrue(SpecialEncounterRules.shouldIgnoreNpcKill(
			12345, SpecialEncounterRules.DOOM_OF_MOKHAIOTL));
		assertEquals(SpecialEncounterRules.SOL_HEREDIT,
			SpecialEncounterRules.rewardClaimSourceForChatMessage(
				SpecialEncounterRules.COLOSSEUM_REWARD_READY_MESSAGE));
		assertEquals(SpecialEncounterRules.SOL_HEREDIT,
			SpecialEncounterRules.rewardClaimSourceForChatMessage("Well done! "
				+ SpecialEncounterRules.COLOSSEUM_REWARD_READY_MESSAGE));
		assertNull(SpecialEncounterRules.rewardClaimSourceForChatMessage("Wave 3 complete"));
		assertTrue(SpecialEncounterRules.isClaimBasedActivity(SpecialEncounterRules.SOL_HEREDIT));
		assertTrue(SpecialEncounterRules.isClaimBasedActivity(SpecialEncounterRules.DOOM_OF_MOKHAIOTL));
		assertEquals(SpecialEncounterRules.SOL_HEREDIT,
			SpecialEncounterRules.rewardClaimSourceForWidget(
				net.runelite.api.gameval.InterfaceID.ColosseumIntermission2.LEFT_BUTTON));
		assertEquals(SpecialEncounterRules.DOOM_OF_MOKHAIOTL,
			SpecialEncounterRules.rewardClaimSourceForWidget(
				net.runelite.api.gameval.InterfaceID.DomEndLevelUi.BTN_CLAIM));
		assertNull(SpecialEncounterRules.rewardClaimSourceForWidget(123));
	}

	@Test
	public void recognisesSharedChestActivityCompanions()
	{
		assertTrue(SpecialEncounterRules.sharesRewardActivity(
			"Dharok the Wretched", "Ahrim the Blighted"));
		assertTrue(SpecialEncounterRules.sharesRewardActivity("Blood Moon", "Eclipse Moon"));
		assertFalse(SpecialEncounterRules.sharesRewardActivity("Blood Moon", "Dharok the Wretched"));
		assertFalse(SpecialEncounterRules.sharesRewardActivity("Goblin", "Goblin"));
		assertTrue(SpecialEncounterRules.isGauntletBoss("Corrupted Hunllef"));
		assertTrue(SpecialEncounterRules.isGauntletRewardSource("Corrupted Hunllef", true));
		assertTrue(SpecialEncounterRules.isGauntletRewardSource("Crystalline Hunllef", false));
		assertFalse(SpecialEncounterRules.isGauntletRewardSource("Crystalline Hunllef", true));
	}

	@Test
	public void ignoresMandatoryTechnicalBossParts()
	{
		assertTrue(SpecialEncounterRules.alwaysIgnoredTechnicalNpc("Respiratory system"));
		assertTrue(SpecialEncounterRules.alwaysIgnoredTechnicalNpc("Enormous Tentacle"));
		assertTrue(SpecialEncounterRules.alwaysIgnoredTechnicalNpc("Abyssal portal"));
		assertTrue(SpecialEncounterRules.alwaysIgnoredTechnicalNpc("Left claw"));
		assertTrue(SpecialEncounterRules.alwaysIgnoredTechnicalNpc("Great Olm (right claw)"));
		assertTrue(SpecialEncounterRules.shouldAlwaysRemainVisible(12345, "Enormous Tentacle"));
		assertFalse(SpecialEncounterRules.alwaysIgnoredTechnicalNpc("Dusk"));
	}

	@Test
	public void identifiesSharedKillBossesWithoutRelaxingOrdinaryMonsters()
	{
		assertTrue(SpecialEncounterRules.allowsSharedKillCredit("Callisto"));
		assertTrue(SpecialEncounterRules.allowsSharedKillCredit("Venenatis"));
		assertTrue(SpecialEncounterRules.allowsSharedKillCredit("Vet'ion"));
		assertFalse(SpecialEncounterRules.allowsSharedKillCredit("Corporeal Beast"));
		assertTrue(SpecialEncounterRules.allowsSharedKillCredit("Nex"));
		assertFalse(SpecialEncounterRules.allowsSharedKillCredit("Goblin"));
	}
}
