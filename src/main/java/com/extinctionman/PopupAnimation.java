package com.extinctionman;

final class PopupAnimation
{
	static final long FADE_IN_MILLIS = 850;
	static final long FADE_OUT_MILLIS = 1250;

	private PopupAnimation() {}

	static float alpha(long nowMillis, long startsAtMillis, long durationMillis)
	{
		long elapsed = nowMillis - startsAtMillis;
		if (elapsed <= 0) return 0.0f;
		if (elapsed < FADE_IN_MILLIS) return elapsed / (float) FADE_IN_MILLIS;
		long remaining = durationMillis - elapsed;
		if (remaining < FADE_OUT_MILLIS)
		{
			return Math.max(0.0f, remaining / (float) FADE_OUT_MILLIS);
		}
		return 1.0f;
	}
}
