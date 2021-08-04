package com.github.javlock.sieve.hub.config;

import lombok.Getter;

public class SieveHubConfig {
	private @Getter SieveHubNetworkConfig networkConfig = new SieveHubNetworkConfig();
	private @Getter SieveHubDataBaseConfig dataBaseConfig = new SieveHubDataBaseConfig();

}
