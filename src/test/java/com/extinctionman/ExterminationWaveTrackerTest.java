package com.extinctionman;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class ExterminationWaveTrackerTest
{
	@Test
	public void onlyKeepsTheExterminatedExactNameVisibleDuringTheWave()
	{
		ExterminationWaveTracker tracker = new ExterminationWaveTracker();
		tracker.start("Goblin", 100);

		assertTrue(tracker.isAnimating("Goblin", 100));
		assertFalse(tracker.isAnimating("Hobgoblin", 100));
		assertTrue(tracker.isAnimating("Goblin", 100 + ExterminationWaveTracker.DURATION_TICKS - 1));
		assertFalse(tracker.isAnimating("Goblin", 100 + ExterminationWaveTracker.DURATION_TICKS));
	}
}
