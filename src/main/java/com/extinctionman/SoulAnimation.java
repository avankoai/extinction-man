package com.extinctionman;

import net.runelite.api.coords.WorldPoint;

final class SoulAnimation
{
	private final WorldPoint worldPoint;
	private final long startedAtMillis;
	private final boolean extinction;
	private final boolean golden;

	SoulAnimation(WorldPoint worldPoint, long startedAtMillis)
	{
		this(worldPoint, startedAtMillis, false);
	}

	SoulAnimation(WorldPoint worldPoint, long startedAtMillis, boolean extinction)
	{
		this(worldPoint, startedAtMillis, extinction, false);
	}

	SoulAnimation(WorldPoint worldPoint, long startedAtMillis, boolean extinction, boolean golden)
	{
		this.worldPoint = worldPoint;
		this.startedAtMillis = startedAtMillis;
		this.extinction = extinction;
		this.golden = golden;
	}

	boolean isExtinction() { return extinction; }
	boolean isGolden() { return golden; }

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
