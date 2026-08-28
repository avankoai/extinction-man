package com.extinctionman;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import javax.inject.Inject;
import javax.inject.Singleton;
import net.runelite.api.Client;
import net.runelite.api.ItemComposition;
import net.runelite.client.game.ItemManager;
import net.runelite.client.util.Text;

@Singleton
final class ItemRegistry
{
	private final Client client;
	private final ItemManager itemManager;
	private final Map<String, WhitelistItemOption> items = new HashMap<>();
	private int cursor;
	private boolean complete;

	@Inject
	ItemRegistry(Client client, ItemManager itemManager)
	{
		this.client = client;
		this.itemManager = itemManager;
	}

	synchronized boolean scanBatch(int batchSize)
	{
		if (complete) return false;
		int itemCount = client.getItemCount();
		if (itemCount <= 0) return false;
		int end = Math.min(itemCount, cursor + batchSize);
		for (; cursor < end; cursor++)
		{
			ItemComposition composition = itemManager.getItemComposition(cursor);
			if (composition == null || composition.getName() == null
				|| "null".equalsIgnoreCase(composition.getName())
				|| composition.getPlaceholderTemplateId() != -1)
			{
				continue;
			}
			int canonicalId = itemManager.canonicalize(composition.getId());
			ItemComposition canonical = itemManager.getItemComposition(canonicalId);
			String cleanName = Text.removeTags(canonical.getName()).trim();
			if (!cleanName.isEmpty())
			{
				items.putIfAbsent(cleanName.toLowerCase(Locale.ENGLISH),
					new WhitelistItemOption(canonicalId, cleanName));
			}
		}
		complete = cursor >= itemCount;
		return complete;
	}

	synchronized List<WhitelistItemOption> search(String query, int limit)
	{
		String normalized = query.trim().toLowerCase(Locale.ENGLISH);
		List<WhitelistItemOption> matches = new ArrayList<>();
		for (WhitelistItemOption item : items.values())
		{
			if (item.getItemName().toLowerCase(Locale.ENGLISH).contains(normalized))
			{
				matches.add(item);
			}
		}
		matches.sort(Comparator
			.comparing((WhitelistItemOption item) -> !item.getItemName().equalsIgnoreCase(query))
			.thenComparing(WhitelistItemOption::getItemName, String.CASE_INSENSITIVE_ORDER)
			.thenComparingInt(WhitelistItemOption::getItemId));
		return matches.subList(0, Math.min(limit, matches.size()));
	}

	synchronized boolean isComplete()
	{
		return complete;
	}
}
