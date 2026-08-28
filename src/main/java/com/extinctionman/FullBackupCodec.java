package com.extinctionman;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

final class FullBackupCodec
{
	private static final String HEADER = "EXTINCTION-MAN-BACKUP-V3";
	private FullBackupCodec() {}

	static String encode(String soulStorage, String metaStorage)
	{
		return HEADER + "\nSOULS=" + encodeText(soulStorage) + "\nMETA=" + encodeText(metaStorage);
	}

	static Decoded decode(String backup)
	{
		if (backup == null || !backup.startsWith(HEADER + "\n"))
		{
			throw new IllegalArgumentException("This is not a current Extinction Man backup.");
		}
		String[] lines = backup.split("\\n");
		if (lines.length != 3 || !lines[1].startsWith("SOULS=") || !lines[2].startsWith("META="))
		{
			throw new IllegalArgumentException("The Extinction Man backup is damaged.");
		}
		try
		{
			String souls = decodeText(lines[1].substring("SOULS=".length()));
			String meta = decodeText(lines[2].substring("META=".length()));
			SoulLogCodec.decodeStorage(souls);
			MetaProgressCodec.decode(meta);
			return new Decoded(souls, meta);
		}
		catch (RuntimeException ex)
		{
			throw new IllegalArgumentException("The Extinction Man backup is damaged.", ex);
		}
	}

	private static String encodeText(String value)
	{
		return Base64.getEncoder().encodeToString(value.getBytes(StandardCharsets.UTF_8));
	}

	private static String decodeText(String value)
	{
		return new String(Base64.getDecoder().decode(value), StandardCharsets.UTF_8);
	}

	static final class Decoded
	{
		private final String soulStorage;
		private final String metaStorage;
		Decoded(String soulStorage, String metaStorage)
		{
			this.soulStorage = soulStorage;
			this.metaStorage = metaStorage;
		}
		String getSoulStorage() { return soulStorage; }
		String getMetaStorage() { return metaStorage; }
	}
}
