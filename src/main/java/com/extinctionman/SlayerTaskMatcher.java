package com.extinctionman;

import java.util.Locale;

final class SlayerTaskMatcher
{
	private SlayerTaskMatcher()
	{
	}

	static boolean matches(String taskName, String exactNpcName)
	{
		if (taskName == null || exactNpcName == null || taskName.trim().isEmpty())
		{
			return false;
		}

		String task = normalize(taskName);
		String npc = normalize(exactNpcName);
		return npc.equals(task) || npc.equals(singularize(task));
	}

	private static String singularize(String task)
	{
		if (task.endsWith("wolves"))
		{
			return task.substring(0, task.length() - "wolves".length()) + "wolf";
		}
		if (task.endsWith("dwarves"))
		{
			return task.substring(0, task.length() - "dwarves".length()) + "dwarf";
		}
		if (task.endsWith("elves"))
		{
			return task.substring(0, task.length() - "elves".length()) + "elf";
		}
		if (task.endsWith("ies") && !task.endsWith("ansies"))
		{
			return task.substring(0, task.length() - 3) + "y";
		}
		return task.endsWith("s") ? task.substring(0, task.length() - 1) : task;
	}

	private static String normalize(String value)
	{
		return value.trim().toLowerCase(Locale.ENGLISH).replace('-', ' ')
			.replaceAll("\\s+", " ");
	}
}
