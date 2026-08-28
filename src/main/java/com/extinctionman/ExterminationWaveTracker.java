package com.extinctionman;

import javax.inject.Singleton;

@Singleton
final class ExterminationWaveTracker
{
	static final int DURATION_TICKS = 6;
	private String npcName;
	private int animationExpiresAtTick;

	void start(String exactNpcName, int currentTick)
	{
		npcName = exactNpcName;
		animationExpiresAtTick = currentTick + DURATION_TICKS;
	}

	boolean isAnimating(String exactNpcName, int currentTick)
	{
		clearIfFinished(currentTick);
		return npcName != null
			&& currentTick < animationExpiresAtTick
			&& npcName.equals(exactNpcName);
	}

	boolean isActive(String exactNpcName, int currentTick)
	{
		clearIfFinished(currentTick);
		return npcName != null && npcName.equals(exactNpcName);
	}

	private void clearIfFinished(int currentTick)
	{
		if (npcName != null && currentTick >= animationExpiresAtTick)
		{
			clear();
		}
	}

	void clear()
	{
		npcName = null;
		animationExpiresAtTick = 0;
	}
}
