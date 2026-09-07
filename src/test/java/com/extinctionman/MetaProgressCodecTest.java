package com.extinctionman;

import java.util.Arrays;
import java.util.Collections;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class MetaProgressCodecTest
{
	@Test
	public void freeItemsKeepZeroCostAfterAcquisitionAndBackup()
	{
		WhitelistUnlock free = new WhitelistUnlock(1, "Test item", "Goblin", false, 0);
		WhitelistUnlock paid = new WhitelistUnlock(2, "Paid item", "Cow", true);
		String encoded = MetaProgressCodec.encode(Collections.emptySet(), Arrays.asList(free.acquired(), paid), 2);
		MetaProgressCodec.Decoded decoded = MetaProgressCodec.decode(encoded);
		assertEquals(0, decoded.getUnlocks().get(0).getPointCost());
		assertTrue(decoded.getUnlocks().get(0).isAcquired());
		assertEquals(100, decoded.getUnlocks().get(1).getPointCost());
		assertEquals(2, decoded.getForfeitedPoints());
		assertEquals(100, MetaProgressCodec.decode(MetaProgressCodec.encode(Collections.emptySet(),
			Collections.singletonList(paid))).getUnlocks().get(0).getPointCost());
	}

	@Test(expected = IllegalArgumentException.class)
	public void invalidStoredItemCostsAreRejected()
	{
		MetaProgressCodec.decode("EM-META-3\nF|0\nW|1|VGVzdA|R29ibGlu|0|-1");
	}
	@Test
	public void roundTripsRewardedSpeciesAndWhitelistHunts()
	{
		WhitelistUnlock active = new WhitelistUnlock(4151, "Abyssal whip", "Abyssal demon", false);
		WhitelistUnlock acquired = new WhitelistUnlock(11840, "Dragon boots", "Spiritual mage", true);
		String encoded = MetaProgressCodec.encode(
			Collections.singleton("Goblin"), Arrays.asList(active, acquired));

		MetaProgressCodec.Decoded decoded = MetaProgressCodec.decode(encoded);
		assertTrue(decoded.getRewardedSpecies().contains("Goblin"));
		assertEquals(2, decoded.getUnlocks().size());
		assertEquals("Abyssal whip", decoded.getUnlocks().get(0).getItemName());
		assertFalse(decoded.getUnlocks().get(0).isAcquired());
		assertTrue(decoded.getUnlocks().get(1).isAcquired());
		assertEquals(0, decoded.getForfeitedPoints());
	}

	@Test
	public void preservesForfeitedPointsAndReadsLegacyData()
	{
		String encoded = MetaProgressCodec.encode(Collections.singleton("Goblin"),
			Collections.emptyList(), 2);
		assertEquals(2, MetaProgressCodec.decode(encoded).getForfeitedPoints());
		assertEquals(0, MetaProgressCodec.decode("EM-META-1\nR|R29ibGlu").getForfeitedPoints());
	}

	@Test
	public void preservesExplicitEditPointCreditsAndReadsOlderDataAsZero()
	{
		String encoded = MetaProgressCodec.encode(Collections.emptySet(), Collections.emptyList(), 0, 3);
		assertEquals(3, MetaProgressCodec.decode(encoded).getBonusPoints());
		assertEquals(0, MetaProgressCodec.decode("EM-META-2\nF|0").getBonusPoints());
	}

	@Test
	public void preservesNegativeBalanceCorrectionOffsets()
	{
		String encoded = MetaProgressCodec.encode(Collections.singleton("Goblin"),
			Collections.emptyList(), 0, -1);
		assertEquals(-1, MetaProgressCodec.decode(encoded).getBonusPoints());
	}

	@Test
	public void migratesOldPointUnitsAndPersistsActivePermit()
	{
		MetaProgressCodec.Decoded legacy = MetaProgressCodec.decode(
			"EM-META-2\nF|1\nW|2|UGFpZCBpdGVt|Q293|1");
		assertEquals(100, legacy.getForfeitedPoints());
		assertEquals(100, legacy.getUnlocks().get(0).getPointCost());

		SoulPermit permit = new SoulPermit("Goblin", false);
		MetaProgressCodec.Decoded current = MetaProgressCodec.decode(MetaProgressCodec.encode(
			Collections.singleton("Goblin"), Collections.emptyList(), 1, 0, permit));
		assertEquals("Goblin", current.getActivePermit().getSourceName());
		assertFalse(current.getActivePermit().isBoss());
	}

	@Test
	public void soulItemMatchesCanonicalIdOrVisibleName()
	{
		WhitelistUnlock unlock = new WhitelistUnlock(100, "Mystic robe top", "Dark wizard", false);
		assertTrue(unlock.matchesItem(100, "Different name"));
		assertTrue(unlock.matchesItem(200, "mystic robe top"));
		assertFalse(unlock.matchesItem(200, "Mystic robe bottom"));
	}

	@Test
	public void persistsMultipleSoulsAndRemainingKills()
	{
		java.util.List<SoulPermit> permits = Arrays.asList(
			new SoulPermit("Goblin", false, 4), new SoulPermit("Vorkath", true, 2));
		MetaProgressCodec.Decoded decoded = MetaProgressCodec.decode(MetaProgressCodec.encode(
			Collections.emptySet(), Collections.emptyList(), 24, 0, permits));
		assertEquals(2, decoded.getActivePermits().size());
		assertEquals(4, decoded.getActivePermits().get(0).getRemainingKills());
		assertEquals(2, decoded.getActivePermits().get(1).getRemainingKills());
	}

}
