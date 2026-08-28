package com.extinctionman;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

final class MetaProgressCodec
{
	private static final String HEADER_V1 = "EM-META-1";
	private static final String HEADER_V2 = "EM-META-2";

	private MetaProgressCodec() {}

	static String encode(Set<String> rewardedSpecies, List<WhitelistUnlock> unlocks)
	{
		return encode(rewardedSpecies, unlocks, 0);
	}

	static String encode(Set<String> rewardedSpecies, List<WhitelistUnlock> unlocks,
		int forfeitedPoints)
	{
		StringBuilder text = new StringBuilder(HEADER_V2)
			.append("\nF|").append(Math.max(0, forfeitedPoints));
		rewardedSpecies.stream().sorted(String.CASE_INSENSITIVE_ORDER)
			.forEach(name -> text.append("\nR|").append(encodeText(name)));
		for (WhitelistUnlock unlock : unlocks)
		{
			text.append("\nW|").append(unlock.getItemId())
				.append('|').append(encodeText(unlock.getItemName()))
				.append('|').append(encodeText(unlock.getSourceNpcName()))
				.append('|').append(unlock.isAcquired() ? '1' : '0');
		}
		return text.toString();
	}

	static Decoded decode(String text)
	{
		String header = text == null ? "" : text.split("\\n", 2)[0];
		if (!HEADER_V1.equals(header) && !HEADER_V2.equals(header))
		{
			throw new IllegalArgumentException("Unsupported Soul Points data version.");
		}
		Set<String> rewarded = new HashSet<>();
		List<WhitelistUnlock> unlocks = new ArrayList<>();
		int forfeitedPoints = 0;
		String[] lines = text.split("\\n");
		try
		{
			for (int index = 1; index < lines.length; index++)
			{
				String[] parts = lines[index].split("\\|", -1);
				if (parts.length == 2 && "F".equals(parts[0]))
				{
					forfeitedPoints = Math.max(0, Integer.parseInt(parts[1]));
				}
				else if (parts.length == 2 && "R".equals(parts[0]))
				{
					rewarded.add(decodeText(parts[1]));
				}
				else if (parts.length == 5 && "W".equals(parts[0]))
				{
					unlocks.add(new WhitelistUnlock(Integer.parseInt(parts[1]),
						decodeText(parts[2]), decodeText(parts[3]), "1".equals(parts[4])));
				}
				else
				{
					throw new IllegalArgumentException();
				}
			}
		}
		catch (RuntimeException ex)
		{
			throw new IllegalArgumentException("The Soul Points data is damaged.", ex);
		}
		return new Decoded(rewarded, unlocks, forfeitedPoints);
	}

	private static String encodeText(String value)
	{
		return Base64.getUrlEncoder().withoutPadding()
			.encodeToString(value.getBytes(StandardCharsets.UTF_8));
	}

	private static String decodeText(String value)
	{
		return new String(Base64.getUrlDecoder().decode(value), StandardCharsets.UTF_8);
	}

	static final class Decoded
	{
		private final Set<String> rewardedSpecies;
		private final List<WhitelistUnlock> unlocks;
		private final int forfeitedPoints;

		Decoded(Set<String> rewardedSpecies, List<WhitelistUnlock> unlocks, int forfeitedPoints)
		{
			this.rewardedSpecies = rewardedSpecies;
			this.unlocks = unlocks;
			this.forfeitedPoints = forfeitedPoints;
		}

		Set<String> getRewardedSpecies() { return rewardedSpecies; }
		List<WhitelistUnlock> getUnlocks() { return unlocks; }
		int getForfeitedPoints() { return forfeitedPoints; }
	}
}
