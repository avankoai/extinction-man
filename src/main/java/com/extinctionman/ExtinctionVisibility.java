package com.extinctionman;

final class ExtinctionVisibility
{
	private ExtinctionVisibility()
	{
	}

	static boolean shouldGhost(boolean extinct, boolean exceptionActive, boolean hidden)
	{
		return extinct && exceptionActive && !hidden;
	}

	static boolean shouldHide(
		String exactNpcName,
		boolean hidingEnabled,
		boolean naturallyExtinct,
		boolean exceptionActive)
	{
		if (!hidingEnabled || exactNpcName == null)
		{
			return false;
		}

		return naturallyExtinct && !exceptionActive;
	}
}
