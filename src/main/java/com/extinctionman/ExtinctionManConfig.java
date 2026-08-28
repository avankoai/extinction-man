package com.extinctionman;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.ConfigSection;

@ConfigGroup(ExtinctionManConfig.GROUP)
public interface ExtinctionManConfig extends Config
{
	String GROUP = "extinctionman";

	@ConfigSection(
		name = "Mandatory kill exception",
		description = "Temporarily allow one exact NPC for a required quest or unlock kill",
		position = 50,
		closedByDefault = true
	)
	String mandatoryExceptionSection = "mandatoryException";

	@ConfigItem(
		keyName = "showOverlay",
		name = "Show soul overlay",
		description = "Show only the last soul-awarding monster and its progress"
	)
	default boolean showOverlay()
	{
		return true;
	}

	@ConfigItem(
		keyName = "showChatMessages",
		name = "Show soul messages",
		description = "Show a chat message when a soul is gained or a monster becomes extinct"
	)
	default boolean showChatMessages()
	{
		return true;
	}

	@ConfigItem(
		keyName = "showSoulAnimations",
		name = "Show soul animations",
		description = "Show a floating soul effect when a valid kill grants a soul",
		position = 2
	)
	default boolean showSoulAnimations()
	{
		return true;
	}

	@ConfigItem(
		keyName = "showExtinctionPopup",
		name = "Show extinction popup",
		description = "Show a large celebration when an NPC reaches 100 souls",
		position = 3
	)
	default boolean showExtinctionPopup()
	{
		return true;
	}

	@ConfigItem(
		keyName = "showSoulPointPopup",
		name = "Show Soul Point popup",
		description = "Show a separate celebration after earning a banked Soul Point",
		position = 4
	)
	default boolean showSoulPointPopup()
	{
		return true;
	}

	@ConfigItem(
		keyName = "hideExtinctNpcs",
		name = "Hide extinct NPCs",
		description = "Visually hide NPCs whose exact name has reached 100 souls"
	)
	default boolean hideExtinctNpcs()
	{
		return true;
	}

	@ConfigItem(
		keyName = "mandatoryExceptionMode",
		name = "Enable exception mode",
		description = "Show the selected extinct NPC but grant no soul and mark its loot forbidden",
		section = mandatoryExceptionSection,
		position = 0
	)
	default boolean mandatoryExceptionMode()
	{
		return false;
	}

	@ConfigItem(
		keyName = "mandatoryExceptionNpc",
		name = "Required NPC exact name",
		description = "The exact visible NPC name required by a quest or other mandatory activity",
		section = mandatoryExceptionSection,
		position = 1
	)
	default String mandatoryExceptionNpc()
	{
		return "";
	}

	@ConfigItem(
		keyName = "allowMandatoryExceptionLoot",
		name = "Allow exception loot",
		description = "Allow loot from the selected mandatory NPC when an item is required",
		section = mandatoryExceptionSection,
		position = 2
	)
	default boolean allowMandatoryExceptionLoot()
	{
		return false;
	}

}
