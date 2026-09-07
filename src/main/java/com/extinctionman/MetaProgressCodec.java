package com.extinctionman;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

final class MetaProgressCodec
{
	private static final String HEADER_V1 = "EM-META-1";
	private static final String HEADER_V2 = "EM-META-2";
	private static final String HEADER_V3 = "EM-META-3";
	private static final String HEADER_V4 = "EM-META-4";
	private static final String HEADER_V5 = "EM-META-5";
	private static final String HEADER_V6 = "EM-META-6";

	private MetaProgressCodec() {}

	static String encode(Set<String> rewardedSpecies, List<WhitelistUnlock> unlocks)
	{
		return encode(rewardedSpecies, unlocks, 0);
	}

	static String encode(Set<String> rewardedSpecies, List<WhitelistUnlock> unlocks,
		int forfeitedPoints)
	{
		return encode(rewardedSpecies, unlocks, forfeitedPoints, 0);
	}

	static String encode(Set<String> rewardedSpecies, List<WhitelistUnlock> unlocks,
		int forfeitedPoints, int bonusPoints)
	{
		return encode(rewardedSpecies, unlocks, forfeitedPoints, bonusPoints,
			Collections.<SoulPermit>emptyList());
	}

	static String encode(Set<String> rewardedSpecies, List<WhitelistUnlock> unlocks,
		int forfeitedPoints, int bonusPoints, SoulPermit activePermit)
	{
		return encode(rewardedSpecies, unlocks, forfeitedPoints, bonusPoints,
			activePermit == null ? Collections.emptyList() : Collections.singletonList(activePermit));
	}

	static String encode(Set<String> rewardedSpecies, List<WhitelistUnlock> unlocks,
		int forfeitedPoints, int bonusPoints, List<SoulPermit> activePermits)
	{
		StringBuilder text = new StringBuilder(HEADER_V6)
			.append("\nF|").append(Math.max(0, forfeitedPoints));
		text.append("\nB|").append(bonusPoints);
		for (SoulPermit permit : activePermits)
		{
			text.append("\nP|").append(permit.isBoss() ? '1' : '0')
				.append('|').append(permit.getRemainingKills())
				.append('|').append(encodeText(permit.getSourceName()));
		}
		rewardedSpecies.stream().sorted(String.CASE_INSENSITIVE_ORDER)
			.forEach(name -> text.append("\nR|").append(encodeText(name)));
		for (WhitelistUnlock unlock : unlocks)
		{
			text.append("\nW|").append(unlock.getItemId())
				.append('|').append(encodeText(unlock.getItemName()))
				.append('|').append(encodeText(unlock.getSourceNpcName()))
				.append('|').append(unlock.isAcquired() ? '1' : '0');
			text.append('|').append(unlock.getPointCost());
		}
		return text.toString();
	}

	static Decoded decode(String text)
	{
		String header = text == null ? "" : text.split("\\n", 2)[0];
		if (!HEADER_V1.equals(header) && !HEADER_V2.equals(header)
			&& !HEADER_V3.equals(header) && !HEADER_V4.equals(header)
			&& !HEADER_V5.equals(header) && !HEADER_V6.equals(header))
		{
			throw new IllegalArgumentException("Unsupported Soul Energy data version.");
		}
		Set<String> rewarded = new HashSet<>();
		List<WhitelistUnlock> unlocks = new ArrayList<>();
		int forfeitedPoints = 0;
		int bonusPoints = 0;
		List<SoulPermit> activePermits = new ArrayList<>();
		boolean legacyEconomy = !HEADER_V5.equals(header) && !HEADER_V6.equals(header);
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
				else if (parts.length == 2 && "B".equals(parts[0])
					&& (HEADER_V4.equals(header) || HEADER_V5.equals(header) || HEADER_V6.equals(header)))
				{
					bonusPoints = Integer.parseInt(parts[1]);
				}
				else if (parts.length == 3 && "P".equals(parts[0]) && HEADER_V5.equals(header))
				{
					activePermits.add(new SoulPermit(decodeText(parts[2]), "1".equals(parts[1])));
				}
				else if (parts.length == 4 && "P".equals(parts[0]) && HEADER_V6.equals(header))
				{
					activePermits.add(new SoulPermit(decodeText(parts[3]), "1".equals(parts[1]),
						Integer.parseInt(parts[2])));
				}
				else if (parts.length == 2 && "R".equals(parts[0]))
				{
					rewarded.add(decodeText(parts[1]));
				}
				else if (parts.length == (HEADER_V3.equals(header) || HEADER_V4.equals(header)
					|| HEADER_V5.equals(header) || HEADER_V6.equals(header) ? 6 : 5)
					&& "W".equals(parts[0]))
				{
					unlocks.add(new WhitelistUnlock(Integer.parseInt(parts[1]),
						decodeText(parts[2]), decodeText(parts[3]), "1".equals(parts[4]),
						HEADER_V3.equals(header) || HEADER_V4.equals(header) || HEADER_V5.equals(header)
							|| HEADER_V6.equals(header)
							? Integer.parseInt(parts[5]) : 1));
				}
				else
				{
					throw new IllegalArgumentException();
				}
			}
		}
		catch (RuntimeException ex)
		{
			throw new IllegalArgumentException("The Soul Energy data is damaged.", ex);
		}
		if (legacyEconomy)
		{
			List<WhitelistUnlock> migrated = new ArrayList<>();
			for (WhitelistUnlock unlock : unlocks)
			{
				migrated.add(new WhitelistUnlock(unlock.getItemId(), unlock.getItemName(),
					unlock.getSourceNpcName(), unlock.isAcquired(), unlock.getPointCost() * 100));
			}
			unlocks = migrated;
			forfeitedPoints *= 100;
			bonusPoints *= 100;
		}
		return new Decoded(rewarded, unlocks, forfeitedPoints, bonusPoints, activePermits,
			legacyEconomy);
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
		private final int bonusPoints;
		private final List<SoulPermit> activePermits;
		private final boolean legacyEconomy;

		Decoded(Set<String> rewardedSpecies, List<WhitelistUnlock> unlocks,
			int forfeitedPoints, int bonusPoints, List<SoulPermit> activePermits, boolean legacyEconomy)
		{
			this.rewardedSpecies = rewardedSpecies;
			this.unlocks = unlocks;
			this.forfeitedPoints = forfeitedPoints;
			this.bonusPoints = bonusPoints;
			this.activePermits = activePermits;
			this.legacyEconomy = legacyEconomy;
		}

		Set<String> getRewardedSpecies() { return rewardedSpecies; }
		List<WhitelistUnlock> getUnlocks() { return unlocks; }
		int getForfeitedPoints() { return forfeitedPoints; }
		int getBonusPoints() { return bonusPoints; }
		SoulPermit getActivePermit() { return activePermits.isEmpty() ? null : activePermits.get(0); }
		List<SoulPermit> getActivePermits() { return new ArrayList<>(activePermits); }
		boolean isLegacyEconomy() { return legacyEconomy; }
	}
}
