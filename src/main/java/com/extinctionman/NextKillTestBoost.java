package com.extinctionman;

import javax.inject.Singleton;

@Singleton
final class NextKillTestBoost
{
	private boolean armed;

	synchronized void arm() { armed = true; }
	synchronized boolean isArmed() { return armed; }
	synchronized boolean consume()
	{
		boolean result = armed;
		armed = false;
		return result;
	}
	synchronized void clear() { armed = false; }
}
