package com.extinctionman;

final class SoulPointAward
{
	private final boolean newExtermination;
	private final int soulEnergyEarned;

	SoulPointAward(boolean newExtermination, int soulEnergyEarned)
	{
		this.newExtermination = newExtermination;
		this.soulEnergyEarned = Math.max(0, soulEnergyEarned);
	}

	boolean isNewExtermination() { return newExtermination; }
	boolean isSoulPointEarned() { return soulEnergyEarned > 0; }
	int getSoulEnergyEarned() { return soulEnergyEarned; }
}
