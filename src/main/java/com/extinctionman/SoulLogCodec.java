package com.extinctionman;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

final class SoulLogCodec
{
	private static final String STORAGE_HEADER = "EM2";
	private static final String BACKUP_HEADER = "EXTINCTION-MAN-BACKUP-V2";

	private SoulLogCodec()
	{
	}

	static String encodeStorage(Map<String, SoulProgress> progress)
	{
		StringBuilder result = new StringBuilder(STORAGE_HEADER);
		new TreeMap<>(progress).forEach((name, value) ->
		{
			if (value.getSouls() > 0)
			{
				result.append('\n').append(encodeName(name)).append('=').append(value.getSouls());
			}
		});
		return result.toString();
	}

	static Map<String, Integer> decodeStorage(String text)
	{
		return decode(text, STORAGE_HEADER);
	}

	static String encodeBackup(Map<String, SoulProgress> progress)
	{
		return BACKUP_HEADER + '\n' + encodeStorage(progress);
	}

	static Map<String, Integer> decodeBackup(String text)
	{
		if (text == null || !text.startsWith(BACKUP_HEADER + "\n"))
		{
			throw new IllegalArgumentException("This is not an Extinction Man backup.");
		}
		return decodeStorage(text.substring(BACKUP_HEADER.length() + 1));
	}

	private static Map<String, Integer> decode(String text, String expectedHeader)
	{
		if (text == null || text.isEmpty() || !text.split("\\n", 2)[0].equals(expectedHeader))
		{
			throw new IllegalArgumentException("Unsupported Extinction Man data version.");
		}
		Map<String, Integer> values = new HashMap<>();
		String[] lines = text.split("\\n");
		for (int index = 1; index < lines.length; index++)
		{
			int separator = lines[index].lastIndexOf('=');
			if (separator <= 0)
			{
				throw new IllegalArgumentException("The Extinction Man data is damaged.");
			}
			try
			{
				String name = decodeName(lines[index].substring(0, separator));
				int souls = Integer.parseInt(lines[index].substring(separator + 1));
				if (name.trim().isEmpty() || souls < 1 || souls > SoulProgress.EXTINCTION_TARGET)
				{
					throw new IllegalArgumentException("The Extinction Man data contains an invalid entry.");
				}
				values.put(name, souls);
			}
			catch (RuntimeException ex)
			{
				throw new IllegalArgumentException("The Extinction Man data is damaged.", ex);
			}
		}
		return values;
	}

	private static String encodeName(String name)
	{
		return Base64.getUrlEncoder().withoutPadding()
			.encodeToString(name.getBytes(StandardCharsets.UTF_8));
	}

	private static String decodeName(String encoded)
	{
		return new String(Base64.getUrlDecoder().decode(encoded), StandardCharsets.UTF_8);
	}
}
