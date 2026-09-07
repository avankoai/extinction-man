package com.extinctionman;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class RewardAccessPolicyTest
{
	@Test
	public void neverBlocksRewardsBeforeExtinction()
	{
		assertFalse(RewardAccessPolicy.blocksSource(false, false, false));
	}

	@Test
	public void blocksExtinctSourceWithoutAValidException()
	{
		assertTrue(RewardAccessPolicy.blocksSource(true, false, false));
	}

	@Test
	public void permitsCompletionRewardOrEligibleSoulItem()
	{
		assertFalse(RewardAccessPolicy.blocksSource(true, true, false));
		assertFalse(RewardAccessPolicy.blocksSource(true, false, true));
	}
}
