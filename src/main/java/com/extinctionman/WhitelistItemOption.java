package com.extinctionman;

final class WhitelistItemOption
{
	private final int itemId;
	private final String itemName;

	WhitelistItemOption(int itemId, String itemName)
	{
		this.itemId = itemId;
		this.itemName = itemName;
	}

	int getItemId() { return itemId; }
	String getItemName() { return itemName; }

	@Override
	public String toString()
	{
		return itemName;
	}
}
