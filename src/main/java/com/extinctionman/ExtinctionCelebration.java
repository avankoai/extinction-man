package com.extinctionman;

final class ExtinctionCelebration
{
	private final String npcName;
	private final long startedAtMillis;
	private final boolean soulPointAwarded;
	private final String completionText;
	private boolean soundClaimed;

	ExtinctionCelebration(String npcName, long startedAtMillis, boolean soulPointAwarded)
	{
		this(npcName, startedAtMillis, soulPointAwarded, "100 / 100 Souls");
	}

	ExtinctionCelebration(String npcName, long startedAtMillis, boolean soulPointAwarded,
		String completionText)
	{
		this.npcName = npcName;
		this.startedAtMillis = startedAtMillis;
		this.soulPointAwarded = soulPointAwarded;
		this.completionText = completionText;
	}

	boolean isSoulPointAwarded()
	{
		return soulPointAwarded;
	}

	String getNpcName()
	{
		return npcName;
	}

	String getCompletionText() { return completionText; }

	boolean claimSound()
	{
		if (soundClaimed) return false;
		soundClaimed = true;
		return true;
	}

	float alpha(long nowMillis, long durationMillis)
	{
		return PopupAnimation.alpha(nowMillis, startedAtMillis, durationMillis);
	}

	boolean isFinished(long nowMillis, long durationMillis)
	{
		return nowMillis - startedAtMillis >= durationMillis;
	}
}
