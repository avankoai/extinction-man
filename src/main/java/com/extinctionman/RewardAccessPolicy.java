package com.extinctionman;

final class RewardAccessPolicy
{
	private RewardAccessPolicy() {}

	static boolean blocksSource(boolean extinct, boolean completionRewardAllowed,
		boolean eligibleSoulItem)
	{
		return extinct && !completionRewardAllowed && !eligibleSoulItem;
	}
}
