package com.extinctionman;

import java.util.concurrent.ScheduledExecutorService;
import javax.inject.Inject;
import javax.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import net.runelite.client.audio.AudioPlayer;

@Slf4j
@Singleton
final class MilestoneAudio
{
	private final AudioPlayer player;
	private final ScheduledExecutorService executor;
	private final ExtinctionManConfig config;
	private volatile long generation;

	@Inject
	MilestoneAudio(AudioPlayer player, ScheduledExecutorService executor, ExtinctionManConfig config)
	{
		this.player = player;
		this.executor = executor;
		this.config = config;
	}

	void update(ExtinctionCelebration extinction, SoulItemFound item)
	{
		if (extinction != null && extinction.claimSound()) play("slayer-level-up.wav");
		if (item != null && item.claimSound()) play("prayer-level-up.wav");
	}

	private void play(String resource)
	{
		int volume = Math.max(0, Math.min(100, config.milestoneVolume()));
		if (!config.playMilestoneSounds() || volume == 0) return;
		long currentGeneration = generation;
		float gain = (float) (20 * Math.log10(volume / 100.0));
		executor.execute(() ->
		{
			if (currentGeneration != generation) return;
			try { player.play(MilestoneAudio.class, resource, gain); }
			catch (Exception e) { log.warn("Unable to play milestone audio {}", resource, e); }
		});
	}

	void clear()
	{
		generation++;
	}
}
