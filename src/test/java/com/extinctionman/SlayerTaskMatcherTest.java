package com.extinctionman;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class SlayerTaskMatcherTest
{
	@Test
	public void matchesPluralTaskToExactNpcName()
	{
		assertTrue(SlayerTaskMatcher.matches("Goblins", "Goblin"));
		assertTrue(SlayerTaskMatcher.matches("Hill giants", "Hill Giant"));
		assertTrue(SlayerTaskMatcher.matches("Aviansies", "Aviansie"));
	}

	@Test
	public void handlesCommonIrregularPlurals()
	{
		assertTrue(SlayerTaskMatcher.matches("Wolves", "Wolf"));
		assertTrue(SlayerTaskMatcher.matches("Dwarves", "Dwarf"));
		assertTrue(SlayerTaskMatcher.matches("Elves", "Elf"));
	}

	@Test
	public void doesNotMatchDifferentNpc()
	{
		assertFalse(SlayerTaskMatcher.matches("Goblins", "Hobgoblin"));
		assertFalse(SlayerTaskMatcher.matches(null, "Goblin"));
	}
}
