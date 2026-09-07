package com.extinctionman;

import java.util.HashMap;
import java.util.Map;
import javax.inject.Singleton;

/** Short-lived proof that the player selected a marked Soul Item ground drop. */
@Singleton
final class SoulItemPickupAuthorization
{
	static final int LIFETIME_TICKS = 5;
	private final Map<Integer, Authorization> authorizations = new HashMap<>();

	void authorize(int itemId, int currentTick)
	{
		authorize(itemId, "", currentTick);
	}

	void authorize(int itemId, String sourceName, int currentTick)
	{
		authorizations.put(itemId, new Authorization(sourceName,
			currentTick + LIFETIME_TICKS));
	}

	boolean consume(int itemId, int currentTick)
	{
		return consumeSource(itemId, currentTick) != null;
	}

	String consumeSource(int itemId, int currentTick)
	{
		Authorization authorization = authorizations.remove(itemId);
		return authorization != null && currentTick <= authorization.expiresAtTick
			? authorization.sourceName : null;
	}

	void clear()
	{
		authorizations.clear();
	}

	private static final class Authorization
	{
		private final String sourceName;
		private final int expiresAtTick;
		private Authorization(String sourceName, int expiresAtTick)
		{
			this.sourceName = sourceName;
			this.expiresAtTick = expiresAtTick;
		}
	}
}
