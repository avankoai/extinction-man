package com.extinctionman;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

public class SoulPointCelebrationTrackerTest
{
	@Test
	public void celebrationWaitsUntilAfterExterminationPopupAndThenExpires()
	{
		SoulPointCelebrationTracker tracker = new SoulPointCelebrationTracker();
		tracker.show(2, 1000);
		assertNull(tracker.active(1000));
		SoulPointCelebration active = tracker.active(1000 + SoulPointCelebrationTracker.DELAY_MILLIS);
		assertNotNull(active);
		assertEquals(2, active.getAvailablePoints());
		assertEquals(true, active.claimSound());
		assertEquals(false, active.claimSound());
		assertEquals(0.0f, active.alpha(1000 + SoulPointCelebrationTracker.DELAY_MILLIS,
			SoulPointCelebrationTracker.DURATION_MILLIS), 0.001f);
		assertNull(tracker.active(1000 + SoulPointCelebrationTracker.DELAY_MILLIS
			+ SoulPointCelebrationTracker.DURATION_MILLIS));
	}
}
