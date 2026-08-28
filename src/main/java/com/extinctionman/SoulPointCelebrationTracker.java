package com.extinctionman;

import javax.inject.Singleton;

@Singleton
final class SoulPointCelebrationTracker
{
	static final long DELAY_MILLIS = ExtinctionCelebrationTracker.DURATION_MILLIS + 250;
	static final long DURATION_MILLIS = 6500;
	private SoulPointCelebration celebration;

	void show(int availablePoints, long nowMillis)
	{
		celebration = new SoulPointCelebration(availablePoints, nowMillis + DELAY_MILLIS);
	}

	SoulPointCelebration active(long nowMillis)
	{
		if (celebration == null) return null;
		if (celebration.isFinished(nowMillis, DURATION_MILLIS))
		{
			celebration = null;
			return null;
		}
		return celebration.hasStarted(nowMillis) ? celebration : null;
	}

	void clear()
	{
		celebration = null;
	}
}
