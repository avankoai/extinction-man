package com.extinctionman;

import java.util.Objects;

final class WhitelistUnlock
{
	private final int itemId;
	private final String itemName;
	private final String sourceNpcName;
	private final boolean acquired;
	private final int pointCost;

	WhitelistUnlock(int itemId, String itemName, String sourceNpcName, boolean acquired)
	{
		this(itemId, itemName, sourceNpcName, acquired, 100);
	}

	WhitelistUnlock(int itemId, String itemName, String sourceNpcName, boolean acquired, int pointCost)
	{
		if (pointCost < 0 || pointCost > 100) throw new IllegalArgumentException("Invalid Unbound Soul cost");
		this.pointCost = pointCost;
		this.itemId = itemId;
		this.itemName = Objects.requireNonNull(itemName);
		this.sourceNpcName = Objects.requireNonNull(sourceNpcName);
		this.acquired = acquired;
	}

	int getItemId() { return itemId; }
	String getItemName() { return itemName; }
	String getSourceNpcName() { return sourceNpcName; }
	boolean isAcquired() { return acquired; }
	int getPointCost() { return pointCost; }

	boolean matchesItem(int canonicalItemId, String cleanItemName)
	{
		return itemId == canonicalItemId || itemName.equalsIgnoreCase(cleanItemName);
	}

	WhitelistUnlock acquired()
	{
		return acquired ? this : new WhitelistUnlock(itemId, itemName, sourceNpcName, true, pointCost);
	}
}
