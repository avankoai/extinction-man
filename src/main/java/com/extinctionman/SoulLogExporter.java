package com.extinctionman;

import java.util.List;

final class SoulLogExporter
{
	private SoulLogExporter()
	{
	}

	static String format(List<SoulProgress> progressEntries)
	{
		StringBuilder text = new StringBuilder("Extinction Man Soul Log\n");
		text.append("Target\tProgress\tStatus\n");
		for (SoulProgress progress : progressEntries)
		{
			String status = progress.isExtinct()
				? "EXTINCT"
				: progress.getRemaining() + " remaining";
			text.append(progress.getNpcName().replace('\t', ' '))
				.append('\t')
				.append(progress.getSouls()).append("/100")
				.append('\t')
				.append(status)
				.append('\n');
		}
		return text.toString();
	}
}
