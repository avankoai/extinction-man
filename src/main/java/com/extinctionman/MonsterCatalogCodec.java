package com.extinctionman;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Collection;
import java.util.Set;
import java.util.TreeSet;

final class MonsterCatalogCodec
{
	private MonsterCatalogCodec() {}

	static String encode(Collection<String> names)
	{
		StringBuilder encoded = new StringBuilder();
		for (String name : new TreeSet<>(names))
		{
			if (encoded.length() > 0) encoded.append(',');
			encoded.append(Base64.getUrlEncoder().withoutPadding()
				.encodeToString(name.getBytes(StandardCharsets.UTF_8)));
		}
		return encoded.toString();
	}

	static Set<String> decode(String encoded)
	{
		Set<String> names = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
		if (encoded == null || encoded.trim().isEmpty()) return names;
		for (String part : encoded.split(","))
		{
			try
			{
				names.add(new String(Base64.getUrlDecoder().decode(part), StandardCharsets.UTF_8));
			}
			catch (IllegalArgumentException ignored)
			{
				// Ignore one damaged catalogue entry instead of losing the complete catalogue.
			}
		}
		return names;
	}
}
