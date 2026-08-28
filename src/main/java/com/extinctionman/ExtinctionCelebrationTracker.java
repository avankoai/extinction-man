package com.extinctionman;

import javax.inject.Singleton;

@Singleton
final class ExtinctionCelebrationTracker
{
	static final long DURATION_MILLIS = 7000;
	private ExtinctionCelebration celebration;

	void show(String npcName, long nowMillis, boolean soulPointAwarded)
	{
		celebration = new ExtinctionCelebration(npcName, nowMillis, soulPointAwarded);
	}

	void showRaid(String raidName, long nowMillis, boolean soulPointAwarded)
	{
		celebration = new ExtinctionCelebration(
			raidName, nowMillis, soulPointAwarded, "100 / 100 Rewards");
	}

	ExtinctionCelebration active(long nowMillis)
	{
		if (celebration != null && celebration.isFinished(nowMillis, DURATION_MILLIS))
		{
			celebration = null;
		}
		return celebration;
	}

	void clear()
	{
		celebration = null;
	}
}
