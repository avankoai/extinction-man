package com.extinctionman;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class MandatoryExceptionMatcherTest
{
	@Test
	public void matchesOnlyTheExactConfiguredNpc()
	{
		assertTrue(MandatoryExceptionMatcher.matches(true, " Goblin ", "Goblin"));
		assertFalse(MandatoryExceptionMatcher.matches(true, "Goblin", "Hobgoblin"));
		assertTrue(MandatoryExceptionMatcher.matches(true, "goblin", "Goblin"));
		assertTrue(MandatoryExceptionMatcher.matches(true, " GoBLin ", "Goblin"));
		assertFalse(MandatoryExceptionMatcher.matches(true, "Gob", "Goblin"));
	}

	@Test
	public void disabledOrEmptyConfigurationNeverMatches()
	{
		assertFalse(MandatoryExceptionMatcher.matches(false, "Goblin", "Goblin"));
		assertFalse(MandatoryExceptionMatcher.matches(true, "", "Goblin"));
		assertFalse(MandatoryExceptionMatcher.matches(true, null, "Goblin"));
		assertFalse(MandatoryExceptionMatcher.matches(true, "Goblin", null));
		assertFalse(MandatoryExceptionMatcher.matches(true, "   ", "Goblin"));
	}
}
