package com.extinctionman;

import net.runelite.api.MenuAction;
import net.runelite.client.util.Text;

final class SoulHuntMenuFilter
{
	static final String SOUL_ITEM_COLOR = "ffb219";
	private SoulHuntMenuFilter() {}

	static boolean isGroundTake(MenuAction action, String option)
	{
		if (option == null || !"Take".equalsIgnoreCase(Text.removeTags(option).trim())) return false;
		return action == MenuAction.GROUND_ITEM_FIRST_OPTION
			|| action == MenuAction.GROUND_ITEM_SECOND_OPTION
			|| action == MenuAction.GROUND_ITEM_THIRD_OPTION
			|| action == MenuAction.GROUND_ITEM_FOURTH_OPTION
			|| action == MenuAction.GROUND_ITEM_FIFTH_OPTION;
	}

	static boolean shouldRemove(boolean forbiddenExceptionLoot, MenuAction action, String option, boolean selectedItem)
	{
		return forbiddenExceptionLoot && !selectedItem && isGroundTake(action, option);
	}

	static String goldenTarget(String target)
	{
		return "<col=" + SOUL_ITEM_COLOR + ">"
			+ Text.removeTags(target == null ? "" : target) + "</col>";
	}
}
