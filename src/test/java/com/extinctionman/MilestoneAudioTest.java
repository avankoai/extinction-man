package com.extinctionman;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import javax.sound.sampled.AudioSystem;
import net.runelite.client.audio.AudioPlayer;
import org.junit.Test;
import static org.junit.Assert.*;

public class MilestoneAudioTest
{
	@Test
	public void playsExtinctionAndSoulItemSoundsOnce() throws Exception
	{
		ScheduledThreadPoolExecutor executor = new ScheduledThreadPoolExecutor(1);
		try
		{
			List<String> played = new ArrayList<>();
			AudioPlayer player = new AudioPlayer() {
				@Override public void play(Class<?> type, String path, float gain) { played.add(path); }
			};
			MilestoneAudio audio = new MilestoneAudio(player, executor, new ExtinctionManConfig() {});
			ExtinctionCelebrationTracker extinction = new ExtinctionCelebrationTracker();
			SoulItemFoundTracker items = new SoulItemFoundTracker();
			extinction.show("Goblin", 0, true);
			for (int i = 0; i < 20; i++) audio.update(extinction.active(i), null);
			executor.submit(() -> {}).get(5, TimeUnit.SECONDS);
			assertEquals(Arrays.asList("slayer-level-up.wav"), played);
			long start = 100;
			items.show("Bones", start + 100);
			for (int i = 0; i < 20; i++) audio.update(null, items.active(start + 100 + i));
			executor.submit(() -> {}).get(5, TimeUnit.SECONDS);
			assertEquals(Arrays.asList("slayer-level-up.wav", "prayer-level-up.wav"), played);
		}
		finally { executor.shutdownNow(); }
	}

	@Test
	public void bundledJinglesArePlayablePcm() throws Exception
	{
		for (String file : Arrays.asList("slayer-level-up.wav", "prayer-level-up.wav"))
		{
			try (javax.sound.sampled.AudioInputStream stream = AudioSystem.getAudioInputStream(
				MilestoneAudio.class.getResource(file)))
			{
				assertEquals(16, stream.getFormat().getSampleSizeInBits());
				assertTrue(stream.getFrameLength() / stream.getFormat().getFrameRate() > 5);
			}
		}
	}
}
