package com.extinctionman;

import javax.inject.Singleton;

@Singleton
final class SoulItemFoundTracker
{
	static final long DURATION_MILLIS = 6500L;
	private SoulItemFound found;

	synchronized void show(String itemName, long nowMillis)
	{
		found = new SoulItemFound(itemName, nowMillis);
	}

	synchronized void preview(String itemName, long nowMillis)
	{
		found = new SoulItemFound(itemName, nowMillis);
	}

	synchronized SoulItemFound active(long nowMillis)
	{
		if (found != null && found.isFinished(nowMillis, DURATION_MILLIS)) found = null;
		return found;
	}

	synchronized void clear()
	{
		found = null;
	}
}
