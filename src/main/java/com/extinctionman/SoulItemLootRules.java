package com.extinctionman;

final class SoulItemLootRules
{
	private SoulItemLootRules() {}

	static boolean allowsActiveHuntItem(WhitelistUnlock activeHunt, String dropSource,
		int canonicalItemId, String itemName)
	{
		return activeHunt != null && !activeHunt.isAcquired() && dropSource != null
			&& activeHunt.getSourceNpcName().equals(dropSource)
			&& activeHunt.matchesItem(canonicalItemId, itemName);
	}
}
