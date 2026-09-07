package com.extinctionman;

import org.junit.Test;
import static org.junit.Assert.*;

public class SoulItemFoundTrackerTest
{
	@Test
	public void sourceVerifiedRewardPopupExpires()
	{
		SoulItemFoundTracker tracker = new SoulItemFoundTracker();
		tracker.show("Soul Hammer", 1000L);
		assertEquals("Soul Hammer", tracker.active(1000L).getItemName());
		assertNull(tracker.active(1000L + SoulItemFoundTracker.DURATION_MILLIS));
	}

	@Test
	public void popupUnfoldsHoldsAndFolds()
	{
		SoulItemFound found = new SoulItemFound("Soul Hammer", 1000L);
		assertEquals(0.0f, found.unfold(1000L, SoulItemFoundTracker.DURATION_MILLIS), 0.001f);
		assertTrue(found.unfold(1100L, SoulItemFoundTracker.DURATION_MILLIS) > 0.0f);
		assertEquals(1.0f, found.unfold(2000L, SoulItemFoundTracker.DURATION_MILLIS), 0.001f);
		assertTrue(found.unfold(1000L + SoulItemFoundTracker.DURATION_MILLIS - 100L,
			SoulItemFoundTracker.DURATION_MILLIS) < 1.0f);
	}

	@Test
	public void allThemedPopupsUseTheSameAnimationTiming()
	{
		long startedAt = 1000L;
		long sampleTime = startedAt + PopupAnimation.UNFOLD_MILLIS / 2;
		long duration = SoulItemFoundTracker.DURATION_MILLIS;
		float itemUnfold = new SoulItemFound("Soul Hammer", startedAt)
			.unfold(sampleTime, duration);
		assertEquals(itemUnfold,
			new ExtinctionCelebration("Goblin", startedAt, false).unfold(sampleTime, duration), 0.001f);
	}

	@Test
	public void previewPlaysTheRewardSoundOnce()
	{
		SoulItemFoundTracker tracker = new SoulItemFoundTracker();
		tracker.preview("Soul Hammer", 1000L);
		assertTrue(tracker.active(1001L).claimSound());
		assertFalse(tracker.active(1002L).claimSound());
		tracker.show("Soul Hammer", 2000L);
		assertTrue(tracker.active(2001L).claimSound());
		assertFalse(tracker.active(2002L).claimSound());
	}
}
