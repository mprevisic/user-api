package com.mprevisic.user.util;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.mprevisic.user.domain.UserBlacklistEntity;
import com.mprevisic.user.repository.UserBlacklistRepository;

/**
 * In-memory user blacklist
 * 
 * @author Marko Previsic
 * @created May 22, 2017
 */
@Component
public class UserBlacklistCache {
	
	private final Logger LOG = LoggerFactory.getLogger(this.getClass()); 

	@Autowired
	private UserBlacklistRepository blacklistRepo;

	private static final long ONE_HOUR = 60 * 60 * 1000L;

	private Map<String, Date> deletedUserMap = new ConcurrentHashMap<>();

	/**
	 * Initializes the cache on server start-up by reading blacklisted users
	 * from DB
	 */
	@PostConstruct
	public void init() {
		LOG.debug("Initializing blacklist cache");
		
		List<UserBlacklistEntity> users = blacklistRepo.findAll();

		users.forEach(u -> deletedUserMap.put(u.getEmail(), new Date(u.getDateTime())));
	}

	/**
	 * Adds a deleted user to the cache
	 */
	public void addDeletedUser(String user) {
		Date now = new Date();

		deletedUserMap.put(user, now);

		UserBlacklistEntity bl = new UserBlacklistEntity();
		bl.setEmail(user);
		bl.setDateTime(now.getTime());

		blacklistRepo.save(bl);
	}

	/**
	 * Checks if user is deleted
	 */
	public boolean checkUserDeleted(String user) {
		return deletedUserMap.containsKey(user);
	}

	/**
	 * Removes user from blacklist
	 */
	public void removeFromCache(String user) {
		deletedUserMap.remove(user);
		blacklistRepo.deleteByEmail(user);
	}

	/**
	 * Clears the cache by removing users deleted more than 1 hour ago to save
	 * memory (access tokens have a TTL of 1 hour so it's unnecessary to hold
	 * them any longer in the cache)
	 */
	@Scheduled(fixedDelay = ONE_HOUR)
	public void clearCache() {
		Date oneHourAgo = new Date(System.currentTimeMillis() - ONE_HOUR);

		List<String> removeList = new ArrayList<>();
		deletedUserMap.forEach((u, d) -> {
			if (d.before(oneHourAgo)) {
				removeList.add(u);
			}
		});

		removeList.forEach(r -> {
			deletedUserMap.remove(r);
			blacklistRepo.deleteByEmail(r);
		});
	}

}
