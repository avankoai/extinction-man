package com.extinctionman;

import java.awt.AlphaComposite;
import java.awt.Composite;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import javax.inject.Inject;
import net.runelite.api.Client;
import net.runelite.api.Perspective;
import net.runelite.api.Point;
import net.runelite.api.coords.LocalPoint;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;

final class SoulAnimationOverlay extends Overlay
{
	private static final BufferedImage SOUL_IMAGE = SoulImageLoader.load(42, 63);
	private final Client client;
	private final ExtinctionManConfig config;
	private final SoulAnimationTracker tracker;

	@Inject
	SoulAnimationOverlay(Client client, ExtinctionManConfig config, SoulAnimationTracker tracker)
	{
		this.client = client;
		this.config = config;
		this.tracker = tracker;
		setPosition(OverlayPosition.DYNAMIC);
		setLayer(OverlayLayer.ABOVE_SCENE);
	}

	@Override
	public Dimension render(Graphics2D graphics)
	{
		if (!config.showSoulAnimations())
		{
			return null;
		}

		long now = System.currentTimeMillis();
		for (SoulAnimation animation : tracker.active(now))
		{
			LocalPoint localPoint = LocalPoint.fromWorld(client, animation.getWorldPoint());
			if (localPoint == null)
			{
				continue;
			}

			double progress = animation.progress(now, SoulAnimationTracker.DURATION_MILLIS);
			int height = 35 + (int) (progress * 55);
			Point imagePoint = Perspective.getCanvasImageLocation(client, localPoint, SOUL_IMAGE, height);
			if (imagePoint == null)
			{
				continue;
			}

			float alpha = (float) Math.max(0.0, 1.0 - progress);
			Composite previousComposite = graphics.getComposite();
			graphics.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
			graphics.drawImage(SOUL_IMAGE, imagePoint.getX(), imagePoint.getY(), null);
			graphics.setComposite(previousComposite);
		}
		return null;
	}

}
