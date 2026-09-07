package com.extinctionman;

import java.util.Arrays;
import java.util.Set;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class MonsterCatalogCodecTest
{
	@Test
	public void roundTripsNamesWithoutDelimiterCollisions()
	{
		Set<String> decoded = MonsterCatalogCodec.decode(MonsterCatalogCodec.encode(
			Arrays.asList("Vet'ion", "TzHaar-Mej — test", "Branda, Queen of Fire")));
		assertEquals(3, decoded.size());
		assertTrue(decoded.contains("Vet'ion"));
		assertTrue(decoded.contains("TzHaar-Mej — test"));
		assertTrue(decoded.contains("Branda, Queen of Fire"));
	}
}
