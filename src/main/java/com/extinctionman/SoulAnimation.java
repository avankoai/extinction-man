package com.extinctionman;

import net.runelite.api.coords.WorldPoint;

final class SoulAnimation
{
	private final WorldPoint worldPoint;
	private final long startedAtMillis;

	SoulAnimation(WorldPoint worldPoint, long startedAtMillis)
	{
		this.worldPoint = worldPoint;
		this.startedAtMillis = startedAtMillis;
	}

	WorldPoint getWorldPoint()
	{
		return worldPoint;
	}

	double progress(long nowMillis, long durationMillis)
	{
		return Math.max(0.0, Math.min(1.0,
			(double) (nowMillis - startedAtMillis) / durationMillis));
	}

	boolean isFinished(long nowMillis, long durationMillis)
	{
		return nowMillis - startedAtMillis >= durationMillis;
	}
}
