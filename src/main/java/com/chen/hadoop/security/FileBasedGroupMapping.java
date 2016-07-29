package com.chen.hadoop.security;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.security.AccessControlException;
import org.apache.hadoop.security.GroupMappingServiceProvider;

public class FileBasedGroupMapping implements GroupMappingServiceProvider {
	
	private static final Log LOG = LogFactory.getLog(FileBasedGroupMapping.class);
	private String fileName;
	Map<String, List<String>> user2group = new HashMap<String, List<String>>();
	private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
	public static final String FILE = "hadoop.security.group.mapping.file";
	
	public FileBasedGroupMapping() {
		Configuration conf = new Configuration();
		fileName = conf.get(FILE,"");
		try {
			cacheGroupsRefresh();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public List<String> getGroups(String user) throws IOException {
		List<String> groups;
		lock.readLock().lock();
		try {
			if(!user2group.containsKey(user)){
				throw new AccessControlException("Permission denied. User " + user + " not found!");
			}
			groups = user2group.get(user);
		} finally {
			lock.readLock().unlock();
		}
		return groups;
	}

	public void cacheGroupsRefresh() throws IOException {
		Map<String, List<String>> user2groupTemp = new HashMap<String, List<String>>();
		readUserGroupMapping(fileName, user2groupTemp);
		
		lock.writeLock().lock();
		try{
			user2group.clear();
			user2group.putAll(user2groupTemp);
		} finally {
			lock.writeLock().unlock();
		}
		LOG.info("user-group mapping refresh success!");
	}

	private void readUserGroupMapping(String fileName, Map<String, List<String>> user2group) throws IOException {
		File file = new File(fileName);
		if (!file.exists()) {
			throw new IOException(fileName+" cannot be found! Please recheck your settings!");
		}
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new FileReader(file));
			String line;
			while ((line = reader.readLine()) != null) {
				// if this is comment line or empty line
				if(line.startsWith("#") || line.trim().isEmpty()){
					continue;
				}
				String[] segs = line.split(":");
				if (segs != null && segs.length == 2) {
					String user = segs[0];
					String[] groups = segs[1].split(",");
					for(int i = 0; i < groups.length; ++i){
						groups[i] = groups[i].trim();
					}
					user2group.put(user, Arrays.asList(groups));
				}
			}
		} finally {
			if (reader != null) {
				reader.close();
			}
		}
	}

	public void cacheGroupsAdd(List<String> groups) throws IOException {
		// does nothing in this provider of user to groups mapping
	}
}
