package com.extinctionman;

import java.util.Locale;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

final class ExtinctionChatMessage
{
	private static final String COLOR = "b84a4a";
	private static final Map<String, String> IRREGULAR_PLURALS = irregularPlurals();

	private ExtinctionChatMessage() {}

	static String random(String npcName)
	{
		return "<col=" + COLOR + ">" + forStyle(npcName,
			ThreadLocalRandom.current().nextInt(3)) + "</col>";
	}

	static String forStyle(String npcName, int style)
	{
		String exactName = npcName == null ? "Unknown creature" : npcName.trim();
		if (usesIndividualWording(exactName))
		{
			return individualMessage(exactName, style);
		}
		String species = exactName.toLowerCase(Locale.ENGLISH);
		String plural = pluralize(species);
		switch (Math.floorMod(style, 3))
		{
			case 0:
				return "The last " + species + " has been slain. "
					+ capitalize(plural) + " are now extinct.";
			case 1:
				return "You just killed the very last " + species
					+ ". The species is gone forever.";
			default:
				return "With that final blow, you have driven " + plural + " to extinction.";
		}
	}

	private static String pluralize(String species)
	{
		int separator = species.lastIndexOf(' ');
		String prefix = separator < 0 ? "" : species.substring(0, separator + 1);
		String finalWord = separator < 0 ? species : species.substring(separator + 1);
		String irregular = IRREGULAR_PLURALS.get(finalWord);
		if (irregular != null) return prefix + irregular;
		if (species.endsWith("ch") || species.endsWith("sh") || species.endsWith("s")
			|| species.endsWith("x") || species.endsWith("z"))
		{
			return species + "es";
		}
		if (species.endsWith("y") && species.length() > 1
			&& "aeiou".indexOf(species.charAt(species.length() - 2)) < 0)
		{
			return species.substring(0, species.length() - 1) + "ies";
		}
		return species + "s";
	}

	private static boolean usesIndividualWording(String npcName)
	{
		if (BossRegistry.isBoss(npcName) || npcName.startsWith("The ") || npcName.indexOf(',') >= 0)
			return true;
		for (int i = 1; i < npcName.length(); i++)
		{
			if (Character.isUpperCase(npcName.charAt(i))) return true;
		}
		return false;
	}

	private static String individualMessage(String npcName, int style)
	{
		String embeddedName = npcName.startsWith("The ")
			? "the " + npcName.substring(4) : npcName;
		switch (Math.floorMod(style, 3))
		{
			case 0:
				return npcName + " has left this world for good.";
			case 1:
				return "You have slain " + embeddedName
					+ " for the final time. This foe is gone forever.";
			default:
				return "With that final blow, " + embeddedName
					+ " has vanished from Gielinor forever.";
		}
	}

	private static Map<String, String> irregularPlurals()
	{
		Map<String, String> plurals = new HashMap<>();
		plurals.put("man", "men");
		plurals.put("woman", "women");
		plurals.put("child", "children");
		plurals.put("person", "people");
		plurals.put("mouse", "mice");
		plurals.put("goose", "geese");
		plurals.put("ox", "oxen");
		plurals.put("sheep", "sheep");
		plurals.put("deer", "deer");
		plurals.put("fish", "fish");
		plurals.put("cyclops", "cyclopes");
		plurals.put("thief", "thieves");
		plurals.put("wolf", "wolves");
		plurals.put("dwarf", "dwarves");
		return plurals;
	}

	private static String capitalize(String text)
	{
		return text.isEmpty() ? text : Character.toUpperCase(text.charAt(0)) + text.substring(1);
	}
}
