package com.extinctionman;

final class SoulPointAward
{
	private final boolean newExtermination;
	private final boolean soulPointEarned;

	SoulPointAward(boolean newExtermination, boolean soulPointEarned)
	{
		this.newExtermination = newExtermination;
		this.soulPointEarned = soulPointEarned;
	}

	boolean isNewExtermination() { return newExtermination; }
	boolean isSoulPointEarned() { return soulPointEarned; }
}
