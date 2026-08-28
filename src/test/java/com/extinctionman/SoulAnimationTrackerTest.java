package com.extinctionman;

import net.runelite.api.coords.WorldPoint;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class SoulAnimationTrackerTest
{
	@Test
	public void animationExpiresAfterItsDuration()
	{
		SoulAnimationTracker tracker = new SoulAnimationTracker();
		tracker.add(new WorldPoint(3200, 3200, 0), 1000);
		assertEquals(1, tracker.active(1000).size());
		assertEquals(1, tracker.active(1000 + SoulAnimationTracker.DURATION_MILLIS - 1).size());
		assertEquals(0, tracker.active(1000 + SoulAnimationTracker.DURATION_MILLIS).size());
	}
}
