package com.extinctionman;

import java.util.Objects;

final class SoulPermit
{
	private final String sourceName;
	private final boolean boss;
	private final int remainingKills;

	SoulPermit(String sourceName, boolean boss)
	{
		this(sourceName, boss, 1);
	}

	SoulPermit(String sourceName, boolean boss, int remainingKills)
	{
		this.sourceName = Objects.requireNonNull(sourceName);
		this.boss = boss;
		if (remainingKills < 1) throw new IllegalArgumentException("A Soul must allow at least one kill");
		this.remainingKills = remainingKills;
	}

	String getSourceName() { return sourceName; }
	boolean isBoss() { return boss; }
	int getRemainingKills() { return remainingKills; }
	int getEnergyPerKill() { return boss ? 10 : 1; }
	int getPointCost() { return getEnergyPerKill() * remainingKills; }

	SoulPermit addKills(int amount)
	{
		return new SoulPermit(sourceName, boss, Math.addExact(remainingKills, amount));
	}

	SoulPermit consumeOne()
	{
		return remainingKills <= 1 ? null : new SoulPermit(sourceName, boss, remainingKills - 1);
	}

	boolean matches(String name)
	{
		return name != null && sourceName.equalsIgnoreCase(name);
	}
}
