package com.extinctionman;

import java.util.Arrays;
import java.util.Collections;
import org.junit.Test;
import static org.junit.Assert.*;

public class SavedProgressLoadingTest
{
	@Test
	public void savedMonstersAreAvailableBeforeAnyDefinitionScan()
	{
		BestiaryRegistry registry = new BestiaryRegistry();
		registry.prioritizeProgress(Arrays.asList(new SoulProgress("Goblin", 45),
			new SoulProgress("Cow", 100), new SoulProgress("Rat", 0)));
		assertFalse(registry.isComplete());
		assertEquals("Goblin", registry.findExactName("goblin"));
		assertEquals("Cow", registry.findExactName("Cow"));
		assertNull(registry.findExactName("Rat"));
		registry.prioritizeProgress(Collections.emptyList());
		assertNull(registry.findExactName("Goblin"));
	}

	@Test
	public void savedItemsCanBeSearchedWithoutClientOrFullScan()
	{
		ItemRegistry registry = new ItemRegistry(null, null);
		registry.prioritizeProgress(Arrays.asList(
			new WhitelistUnlock(4151, "Abyssal whip", "Abyssal demon", false),
			new WhitelistUnlock(11840, "Dragon boots", "Spiritual mage", true)));
		assertFalse(registry.isComplete());
		assertEquals(4151, registry.search("whip", 50).get(0).getItemId());
		assertEquals(11840, registry.search("dragon", 50).get(0).getItemId());
		registry.prioritizeProgress(Collections.emptyList());
		assertTrue(registry.search("whip", 50).isEmpty());
	}
}
