package com.extinctionman;

import java.util.IdentityHashMap;
import java.util.Map;
import javax.inject.Singleton;
import net.runelite.api.Model;
import net.runelite.api.NPC;

@Singleton
final class ExtinctGhostModelStyler
{
	private final Map<Model, OriginalModelColors> originals = new IdentityHashMap<>();

	void style(NPC npc)
	{
		Model model = npc.getModel();
		if (model == null || originals.containsKey(model))
		{
			return;
		}
		int[] colors1 = model.getFaceColors1();
		int[] colors2 = model.getFaceColors2();
		int[] colors3 = model.getFaceColors3();
		byte[] transparencies = model.getFaceTransparencies();
		originals.put(model, new OriginalModelColors(
			colors1.clone(), colors2.clone(), colors3.clone(),
			transparencies == null ? null : transparencies.clone()));
		cool(colors1);
		cool(colors2);
		cool(colors3);
		if (transparencies != null)
		{
			for (int i = 0; i < transparencies.length; i++)
			{
				int current = Byte.toUnsignedInt(transparencies[i]);
				if (current < 28)
				{
					transparencies[i] = 28;
				}
			}
		}
	}

	void restore()
	{
		for (Map.Entry<Model, OriginalModelColors> entry : originals.entrySet())
		{
			Model model = entry.getKey();
			OriginalModelColors original = entry.getValue();
			copy(original.colors1, model.getFaceColors1());
			copy(original.colors2, model.getFaceColors2());
			copy(original.colors3, model.getFaceColors3());
			if (original.transparencies != null && model.getFaceTransparencies() != null)
			{
				copy(original.transparencies, model.getFaceTransparencies());
			}
		}
		originals.clear();
	}

	static int spectralColor(int color)
	{
		if (color < 0)
		{
			return color;
		}
		int saturation = (color >> 7) & 7;
		int luminance = color & 127;
		int spectralHue = 39;
		int spectralSaturation = Math.max(1, Math.min(3, saturation));
		return (spectralHue << 10) | (spectralSaturation << 7) | luminance;
	}

	private static void cool(int[] colors)
	{
		for (int i = 0; i < colors.length; i++)
		{
			colors[i] = spectralColor(colors[i]);
		}
	}

	private static void copy(int[] source, int[] target)
	{
		System.arraycopy(source, 0, target, 0, Math.min(source.length, target.length));
	}

	private static void copy(byte[] source, byte[] target)
	{
		System.arraycopy(source, 0, target, 0, Math.min(source.length, target.length));
	}

	private static final class OriginalModelColors
	{
		private final int[] colors1;
		private final int[] colors2;
		private final int[] colors3;
		private final byte[] transparencies;

		private OriginalModelColors(int[] colors1, int[] colors2, int[] colors3,
			byte[] transparencies)
		{
			this.colors1 = colors1;
			this.colors2 = colors2;
			this.colors3 = colors3;
			this.transparencies = transparencies;
		}
	}
}
