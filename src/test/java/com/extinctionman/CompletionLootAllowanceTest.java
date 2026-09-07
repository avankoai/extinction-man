package com.extinctionman;

import java.lang.reflect.Proxy;
import net.runelite.api.NPC;
import org.junit.Test;
import static org.junit.Assert.*;

public class CompletionLootAllowanceTest
{
	@Test
	public void extinctionKillLootIsAllowedExactlyOnceByNpcIdentity()
	{
		CompletionLootAllowance allowance = new CompletionLootAllowance();
		NPC completionKill = npc();
		NPC laterExceptionKill = npc();
		allowance.mark(completionKill);
		assertFalse(allowance.consume(laterExceptionKill));
		assertTrue(allowance.consume(completionKill));
		assertFalse(allowance.consume(completionKill));
	}

	private static NPC npc()
	{
		return (NPC) Proxy.newProxyInstance(NPC.class.getClassLoader(), new Class<?>[]{NPC.class},
			(proxy, method, args) -> { throw new AssertionError("NPC value equality must not be used"); });
	}
}
