package com.extinctionman;

import java.util.HashSet;
import java.util.Set;

final class SoulItemPopupGate
{
	private final Set<Integer> shownItems = new HashSet<>();

	boolean claim(int itemId)
	{
		return shownItems.add(itemId);
	}

	void release(int itemId)
	{
		shownItems.remove(itemId);
	}

	void clear()
	{
		shownItems.clear();
	}
}
