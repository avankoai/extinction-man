package com.extinctionman;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import net.runelite.api.NPC;
import net.runelite.api.Renderable;
import org.junit.Test;
import static org.junit.Assert.assertTrue;

public class ExterminationPendingVisibilityTest
{
	@Test
	public void nearbyNpcStaysVisibleBeforeDeathAnimationArrives() throws Exception
	{
		ExtinctionManPlugin plugin = new ExtinctionManPlugin();
		NPC victim = npc();
		NPC nearby = npc();
		set(plugin, "repository", new SoulRepository(null));
		set(plugin, "pendingWaveNpc", victim);
		set(plugin, "pendingWaveNpcName", "Goblin");
		Method draw = ExtinctionManPlugin.class.getDeclaredMethod("shouldDraw", Renderable.class, boolean.class);
		draw.setAccessible(true);
		assertTrue((boolean) draw.invoke(plugin, nearby, false));
		assertTrue((boolean) draw.invoke(plugin, victim, false));
	}

	@Test
	public void permitOrbRemainsUntilThePendingDeathWaveStarts() throws Exception
	{
		ExtinctionManPlugin plugin = new ExtinctionManPlugin();
		NPC victim = npc();
		NPC nearby = npc();
		set(plugin, "pendingWaveNpc", victim);
		set(plugin, "pendingWaveNpcName", "Goblin");
		set(plugin, "pendingWavePermit", true);
		Method ghost = ExtinctionManPlugin.class.getDeclaredMethod("isExtinctGhost", NPC.class);
		ghost.setAccessible(true);
		assertTrue((boolean) ghost.invoke(plugin, victim));
		assertTrue((boolean) ghost.invoke(plugin, nearby));
	}

	private static NPC npc()
	{
		return (NPC) Proxy.newProxyInstance(NPC.class.getClassLoader(), new Class<?>[]{NPC.class},
			(proxy, method, args) -> {
				if (method.getName().equals("getName")) return "Goblin";
				throw new AssertionError("Unexpected NPC call: " + method.getName());
			});
	}

	private static void set(Object object, String name, Object value) throws Exception
	{
		Field field = object.getClass().getDeclaredField(name);
		field.setAccessible(true);
		field.set(object, value);
	}
}
