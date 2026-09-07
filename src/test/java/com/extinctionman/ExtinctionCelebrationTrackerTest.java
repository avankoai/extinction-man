package com.extinctionman;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

public class ExtinctionCelebrationTrackerTest
{
	@Test
	public void celebrationExpiresAndFades()
	{
		ExtinctionCelebrationTracker tracker = new ExtinctionCelebrationTracker();
		tracker.show("Goblin", 1000, true);
		ExtinctionCelebration active = tracker.active(1000);
		assertNotNull(active);
		assertEquals(true, active.isSoulPointAwarded());
		assertEquals(true, active.claimSound());
		assertEquals(false, active.claimSound());
		assertEquals(0.0f, active.alpha(1000, ExtinctionCelebrationTracker.DURATION_MILLIS), 0.001f);
		assertEquals(0.5f, active.alpha(1000 + PopupAnimation.FADE_IN_MILLIS / 2,
			ExtinctionCelebrationTracker.DURATION_MILLIS), 0.01f);
		assertEquals(1.0f, active.alpha(1000 + PopupAnimation.FADE_IN_MILLIS,
			ExtinctionCelebrationTracker.DURATION_MILLIS), 0.001f);
		assertNull(tracker.active(1000 + ExtinctionCelebrationTracker.DURATION_MILLIS));
	}

	@Test
	public void everyExtinctionCelebrationShowsTheCollectedPoint()
	{
		ExtinctionCelebrationTracker tracker = new ExtinctionCelebrationTracker();
		tracker.showRaid(SpecialEncounterRules.TOMBS_OF_AMASCUT, 1000, false);
		assertEquals("10 Soul Energy Collected", tracker.active(1000).getCompletionText());
	}

	@Test
	public void previewLeavesTheSoundReadyToPlay()
	{
		ExtinctionCelebrationTracker tracker = new ExtinctionCelebrationTracker();
		tracker.preview("Goblin", 1000L);
		assertEquals(true, tracker.active(1001L).claimSound());
		assertEquals(false, tracker.active(1002L).claimSound());
	}
}
