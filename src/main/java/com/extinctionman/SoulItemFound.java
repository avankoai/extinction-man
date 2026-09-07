package com.extinctionman;

final class SoulItemFound
{
	private final String itemName;
	private final long startedAtMillis;
	private boolean soundClaimed;

	SoulItemFound(String itemName, long startedAtMillis)
	{
		this.itemName = itemName;
		this.startedAtMillis = startedAtMillis;
	}

	String getItemName() { return itemName; }

	float alpha(long nowMillis, long durationMillis)
	{
		return PopupAnimation.alpha(nowMillis, startedAtMillis, durationMillis);
	}

	float unfold(long nowMillis, long durationMillis)
	{
		return PopupAnimation.unfold(nowMillis, startedAtMillis, durationMillis);
	}

	boolean claimSound()
	{
		if (soundClaimed) return false;
		soundClaimed = true;
		return true;
	}

	boolean isFinished(long nowMillis, long durationMillis)
	{
		return nowMillis - startedAtMillis >= durationMillis;
	}
}
