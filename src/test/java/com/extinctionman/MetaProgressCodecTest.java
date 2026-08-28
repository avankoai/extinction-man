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
	public void soulItemMatchesCanonicalIdOrVisibleName()
	{
		WhitelistUnlock unlock = new WhitelistUnlock(100, "Mystic robe top", "Dark wizard", false);
		assertTrue(unlock.matchesItem(100, "Different name"));
		assertTrue(unlock.matchesItem(200, "mystic robe top"));
		assertFalse(unlock.matchesItem(200, "Mystic robe bottom"));
	}

	@Test
	public void spectralColorIsStableWhenAppliedRepeatedly()
	{
		int original = (12 << 10) | (6 << 7) | 54;
		int spectral = ExtinctGhostModelStyler.spectralColor(original);
		assertEquals(spectral, ExtinctGhostModelStyler.spectralColor(spectral));
		assertEquals(original & 127, spectral & 127);
	}
}
