package com.extinctionman;

final class ExtinctionVisibility
{
	private ExtinctionVisibility()
	{
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
