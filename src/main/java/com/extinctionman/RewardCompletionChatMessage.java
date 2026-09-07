package com.extinctionman;

import java.util.concurrent.ThreadLocalRandom;

final class RewardCompletionChatMessage
{
	private static final String COLOR = "b84a4a";

	private RewardCompletionChatMessage() {}

	static String random(String activityName)
	{
		return "<col=" + COLOR + ">" + forStyle(activityName,
			ThreadLocalRandom.current().nextInt(3)) + "</col>";
	}

	static String forStyle(String activityName, int style)
	{
		switch (Math.floorMod(style, 3))
		{
			case 0:
				return "After claiming the 100th reward from " + activityName
					+ ", you have a feeling you have found everything this place has to offer.";
			case 1:
				return "The 100th reward chest has been opened. " + activityName
					+ " holds no more secrets for you.";
			default:
				return "With this final reward, you have exhausted everything "
					+ activityName + " can offer.";
		}
	}
}
