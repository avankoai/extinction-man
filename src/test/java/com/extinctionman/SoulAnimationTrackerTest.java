package com.extinctionman;

import net.runelite.api.coords.WorldPoint;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class SoulAnimationTrackerTest
{
	@Test
	public void extinctionOrbsShareTimingAndDoNotChangeNormalOrbs()
	{
		SoulAnimationTracker tracker = new SoulAnimationTracker();
		tracker.add(new WorldPoint(3200, 3200, 0), 500);
		tracker.add(new WorldPoint(3201, 3200, 0), 1000, true);
		tracker.add(new WorldPoint(3202, 3200, 0), 1000, true);
		tracker.addGolden(new WorldPoint(3203, 3200, 0), 1000);
		java.util.List<SoulAnimation> active = tracker.active(1100);
		org.junit.Assert.assertFalse(active.get(0).isExtinction());
		org.junit.Assert.assertTrue(active.get(1).isExtinction());
		org.junit.Assert.assertTrue(active.get(2).isExtinction());
		org.junit.Assert.assertFalse(active.get(2).isGolden());
		org.junit.Assert.assertTrue(active.get(3).isGolden());
		assertEquals(active.get(1).progress(1100, 1400), active.get(2).progress(1100, 1400), 0.0);
		assertEquals(0, tracker.active(2400).size());
	}
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
