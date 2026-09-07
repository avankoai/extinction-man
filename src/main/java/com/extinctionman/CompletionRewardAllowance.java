package com.extinctionman;

final class CompletionRewardAllowance
{
	private String pendingSource;
	private String activeSource;

	void allow(String source, String openRewardSource)
	{
		if (source != null && source.equals(openRewardSource))
		{
			activeSource = source;
			pendingSource = null;
		}
		else
		{
			pendingSource = source;
		}
	}

	void claim(String source)
	{
		if (source != null && source.equals(pendingSource))
		{
			activeSource = source;
			pendingSource = null;
		}
	}

	boolean allows(String source)
	{
		return source != null && (source.equals(pendingSource) || source.equals(activeSource));
	}

	void close(String source)
	{
		if (source != null && source.equals(activeSource)) activeSource = null;
	}

	void clear()
	{
		pendingSource = null;
		activeSource = null;
	}
}
