package com.extinctionman;

import java.util.Objects;

final class SoulProgress
{
	static final int EXTINCTION_TARGET = 100;

	private final String npcName;
	private final int souls;

	SoulProgress(String npcName, int souls)
	{
		this.npcName = Objects.requireNonNull(npcName);
		this.souls = Math.max(0, Math.min(EXTINCTION_TARGET, souls));
	}

	String getNpcName()
	{
		return npcName;
	}

	int getSouls()
	{
		return souls;
	}

	boolean isExtinct()
	{
		return souls >= EXTINCTION_TARGET;
	}

	int getRemaining()
	{
		return Math.max(0, EXTINCTION_TARGET - souls);
	}

	@Override
	public String toString()
	{
		return npcName + " — " + souls + "/100 souls";
	}

	SoulProgress gainSoul()
	{
		return isExtinct() ? this : new SoulProgress(npcName, souls + 1);
	}
}
