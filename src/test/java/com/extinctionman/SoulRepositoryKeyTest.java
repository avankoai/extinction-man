package com.extinctionman;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class SoulRepositoryKeyTest
{
	@Test
	public void encodedKeyRoundTripsExactUnicodeName()
	{
		String name = "TzHaar-Mej — test";
		String fullKey = ExtinctionManConfig.GROUP + "." + SoulRepository.key(name);
		assertEquals(name, SoulRepository.nameFromFullKey(fullKey));
	}

	@Test
	public void unrelatedOrInvalidKeysAreIgnored()
	{
		assertNull(SoulRepository.nameFromFullKey("other.souls_R29ibGlu"));
		assertNull(SoulRepository.nameFromFullKey(ExtinctionManConfig.GROUP + ".souls_%%%"));
	}
}
