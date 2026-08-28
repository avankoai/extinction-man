package com.extinctionman;

import java.util.Objects;

final class WhitelistUnlock
{
	private final int itemId;
	private final String itemName;
	private final String sourceNpcName;
	private final boolean acquired;

	WhitelistUnlock(int itemId, String itemName, String sourceNpcName, boolean acquired)
	{
		this.itemId = itemId;
		this.itemName = Objects.requireNonNull(itemName);
		this.sourceNpcName = Objects.requireNonNull(sourceNpcName);
		this.acquired = acquired;
	}

	int getItemId() { return itemId; }
	String getItemName() { return itemName; }
	String getSourceNpcName() { return sourceNpcName; }
	boolean isAcquired() { return acquired; }

	boolean matchesItem(int canonicalItemId, String cleanItemName)
	{
		return itemId == canonicalItemId || itemName.equalsIgnoreCase(cleanItemName);
	}

	WhitelistUnlock acquired()
	{
		return acquired ? this : new WhitelistUnlock(itemId, itemName, sourceNpcName, true);
	}
}
