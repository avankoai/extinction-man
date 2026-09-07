package com.extinctionman;

final class PopupAnimation
{
	static final long FADE_IN_MILLIS = 850;
	static final long FADE_OUT_MILLIS = 1250;
	static final long UNFOLD_MILLIS = 850;
	static final long FOLD_MILLIS = 900;

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

	static float unfold(long nowMillis, long startsAtMillis, long durationMillis)
	{
		return unfold(nowMillis, startsAtMillis, durationMillis, UNFOLD_MILLIS, FOLD_MILLIS);
	}

	static float unfold(long nowMillis, long startsAtMillis, long durationMillis,
		long unfoldMillis, long foldMillis)
	{
		long elapsed = nowMillis - startsAtMillis;
		if (elapsed <= 0 || elapsed >= durationMillis) return 0.0f;
		if (elapsed < unfoldMillis)
		{
			return smooth(elapsed / (float) unfoldMillis);
		}
		long remaining = durationMillis - elapsed;
		if (remaining < foldMillis)
		{
			return smooth(remaining / (float) foldMillis);
		}
		return 1.0f;
	}

	private static float smooth(float value)
	{
		float clamped = Math.max(0.0f, Math.min(1.0f, value));
		return clamped * clamped * (3.0f - 2.0f * clamped);
	}
}
