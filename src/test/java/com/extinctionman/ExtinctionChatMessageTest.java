package com.extinctionman;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class ExtinctionChatMessageTest
{
	@Test
	public void providesAllThreeCowExtinctionMessages()
	{
		assertEquals("The last cow has been slain. Cows are now extinct.",
			ExtinctionChatMessage.forStyle("Cow", 0));
		assertEquals("You just killed the very last cow. The species is gone forever.",
			ExtinctionChatMessage.forStyle("Cow", 1));
		assertEquals("With that final blow, you have driven cows to extinction.",
			ExtinctionChatMessage.forStyle("Cow", 2));
	}

	@Test
	public void pluralizesCommonSpeciesNames()
	{
		assertEquals("The last banshee has been slain. Banshees are now extinct.",
			ExtinctionChatMessage.forStyle("Banshee", 0));
		assertEquals("With that final blow, you have driven monkeys to extinction.",
			ExtinctionChatMessage.forStyle("Monkey", 2));
		assertEquals("The last man has been slain. Men are now extinct.",
			ExtinctionChatMessage.forStyle("Man", 0));
		assertEquals("With that final blow, you have driven cave goblin men to extinction.",
			ExtinctionChatMessage.forStyle("Cave goblin man", 2));
	}

	@Test
	public void avoidsInventingPluralsForNamedCreatures()
	{
		assertEquals("Great Olm has left this world for good.",
			ExtinctionChatMessage.forStyle("Great Olm", 0));
		assertEquals("You have slain the Nightmare for the final time. This foe is gone forever.",
			ExtinctionChatMessage.forStyle("The Nightmare", 1));
	}
}
