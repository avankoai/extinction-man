package com.extinctionman;

final class MandatoryExceptionMatcher
{
	private MandatoryExceptionMatcher()
	{
	}

	static boolean matches(boolean enabled, String configuredNpc, String exactNpcName)
	{
		return enabled
			&& configuredNpc != null
			&& exactNpcName != null
			&& !configuredNpc.trim().isEmpty()
			&& exactNpcName.equalsIgnoreCase(configuredNpc.trim());
	}
}
