package com.extinctionman;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
final class WhitelistHuntService
{
	private final MetaProgressRepository metaProgressRepository;

	@Inject
	WhitelistHuntService(MetaProgressRepository metaProgressRepository)
	{
		this.metaProgressRepository = metaProgressRepository;
	}

	synchronized WhitelistUnlock getActiveHunt()
	{
		return metaProgressRepository.getActiveHunt();
	}

	synchronized boolean completeActiveHunt()
	{
		return metaProgressRepository.completeActiveHunt();
	}

	synchronized boolean isWhitelisted(int canonicalItemId)
	{
		return metaProgressRepository.isWhitelisted(canonicalItemId);
	}

	synchronized boolean isWhitelisted(int canonicalItemId, String itemName)
	{
		return metaProgressRepository.isWhitelisted(canonicalItemId, itemName);
	}
}
