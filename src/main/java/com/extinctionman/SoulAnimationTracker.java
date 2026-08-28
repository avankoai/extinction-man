package com.extinctionman;

import java.util.ArrayList;
import java.util.List;
import javax.inject.Singleton;
import net.runelite.api.coords.WorldPoint;

@Singleton
final class SoulAnimationTracker
{
	static final long DURATION_MILLIS = 1400;
	private final List<SoulAnimation> animations = new ArrayList<>();

	void add(WorldPoint worldPoint, long nowMillis)
	{
		if (worldPoint != null)
		{
			animations.add(new SoulAnimation(worldPoint, nowMillis));
		}
	}

	List<SoulAnimation> active(long nowMillis)
	{
		animations.removeIf(animation -> animation.isFinished(nowMillis, DURATION_MILLIS));
		return new ArrayList<>(animations);
	}

	void clear()
	{
		animations.clear();
	}
}
