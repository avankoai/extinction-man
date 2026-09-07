package com.extinctionman;

import net.runelite.api.MenuAction;
import org.junit.Test;
import static org.junit.Assert.*;

public class SoulHuntMenuFilterTest
{
	@Test
	public void removesOnlyForbiddenExceptionLootAndKeepsSelectedItem()
	{
		for (MenuAction action : new MenuAction[]{MenuAction.GROUND_ITEM_FIRST_OPTION,
			MenuAction.GROUND_ITEM_SECOND_OPTION, MenuAction.GROUND_ITEM_THIRD_OPTION,
			MenuAction.GROUND_ITEM_FOURTH_OPTION, MenuAction.GROUND_ITEM_FIFTH_OPTION})
		{
			assertTrue(SoulHuntMenuFilter.shouldRemove(true, action, "Take", false));
			assertFalse(SoulHuntMenuFilter.shouldRemove(true, action, "Take", true));
			assertFalse(SoulHuntMenuFilter.shouldRemove(false, action, "Take", false));
		}
	}

	@Test
	public void preservesOtherActionsAndRecognisesColouredTakeText()
	{
		assertFalse(SoulHuntMenuFilter.isGroundTake(MenuAction.WALK, "Take"));
		assertFalse(SoulHuntMenuFilter.isGroundTake(MenuAction.GROUND_ITEM_THIRD_OPTION, "Examine"));
		assertFalse(SoulHuntMenuFilter.isGroundTake(MenuAction.GROUND_ITEM_THIRD_OPTION, null));
		assertTrue(SoulHuntMenuFilter.isGroundTake(MenuAction.GROUND_ITEM_THIRD_OPTION, "<col=ff0000>Take</col>"));
	}

	@Test
	public void soulItemTargetUsesGoldenOrbColour()
	{
		assertEquals("<col=ffb219>Hammer</col>",
			SoulHuntMenuFilter.goldenTarget("<col=ff9040>Hammer</col>"));
	}
}
