package com.extinctionman;

final class SoulPointCelebration
{
	private final int availablePoints;
	private final long startsAtMillis;
	private boolean soundClaimed;

	SoulPointCelebration(int availablePoints, long startsAtMillis)
	{
		this.availablePoints = availablePoints;
		this.startsAtMillis = startsAtMillis;
	}

	int getAvailablePoints() { return availablePoints; }
	boolean claimSound()
	{
		if (soundClaimed) return false;
		soundClaimed = true;
		return true;
	}
	boolean hasStarted(long nowMillis) { return nowMillis >= startsAtMillis; }
	boolean isFinished(long nowMillis, long durationMillis)
	{
		return nowMillis - startsAtMillis >= durationMillis;
	}
	float alpha(long nowMillis, long durationMillis)
	{
		return PopupAnimation.alpha(nowMillis, startsAtMillis, durationMillis);
	}
}
