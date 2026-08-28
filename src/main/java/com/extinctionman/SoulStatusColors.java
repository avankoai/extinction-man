package com.extinctionman;

import java.awt.Color;

final class SoulStatusColors
{
	static final Color COMPLETE = new Color(75, 205, 95);
	static final Color IN_PROGRESS = new Color(255, 170, 45);
	static final Color NOT_STARTED = new Color(220, 80, 80);

	private SoulStatusColors()
	{
	}

	static Color forProgress(SoulProgress progress)
	{
		if (progress.isExtinct())
		{
			return COMPLETE;
		}
		return progress.getSouls() > 0 ? IN_PROGRESS : NOT_STARTED;
	}
}
