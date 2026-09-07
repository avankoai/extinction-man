package com.extinctionman;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class RewardCompletionChatMessageTest
{
	@Test
	public void providesThreeRewardChestCompletionMessages()
	{
		String activity = "Chambers of Xeric";
		assertEquals("After claiming the 100th reward from Chambers of Xeric, "
			+ "you have a feeling you have found everything this place has to offer.",
			RewardCompletionChatMessage.forStyle(activity, 0));
		assertEquals("The 100th reward chest has been opened. Chambers of Xeric "
			+ "holds no more secrets for you.", RewardCompletionChatMessage.forStyle(activity, 1));
		assertEquals("With this final reward, you have exhausted everything "
			+ "Chambers of Xeric can offer.", RewardCompletionChatMessage.forStyle(activity, 2));
	}
}
