package com.extinctionman;

import java.util.HashMap;
import java.util.Map;
import javax.inject.Singleton;

/** Links an immediate inventory or overflow-ground reward to a verified activity action. */
@Singleton
final class SoulItemRewardAuthorization
{
	static final int LIFETIME_TICKS = 30;
	private final Map<Integer, Authorization> authorizations = new HashMap<>();

	void authorize(int itemId, String source, int currentTick)
	{
		authorizations.put(itemId, new Authorization(source, currentTick + LIFETIME_TICKS));
	}

	boolean matches(int itemId, int currentTick)
	{
		Authorization authorization = authorizations.get(itemId);
		return authorization != null && currentTick <= authorization.expiresAtTick;
	}

	String source(int itemId, int currentTick)
	{
		Authorization authorization = authorizations.get(itemId);
		return authorization != null && currentTick <= authorization.expiresAtTick
			? authorization.sourceName : null;
	}

	String consume(int itemId, int currentTick)
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
