package com.extinctionman;

final class SoulPointEconomy
{
	static final int EXTERMINATIONS_PER_POINT = 100;

	private SoulPointEconomy() {}

	static int earnedPoints(int completedExterminations)
	{
		return Math.max(0, completedExterminations) / EXTERMINATIONS_PER_POINT;
	}

	static int availablePoints(int completedExterminations, int unlocksSpent)
	{
		return Math.max(0, earnedPoints(completedExterminations) - Math.max(0, unlocksSpent));
	}

	static int progressToNextPoint(int completedExterminations)
	{
		return Math.max(0, completedExterminations) % EXTERMINATIONS_PER_POINT;
	}
}
