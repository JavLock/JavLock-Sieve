package com.github.javlock.sieve.hub.config;

import lombok.Getter;
import lombok.Setter;

public class SieveHubDataBaseConfig {
	private @Getter @Setter String host = "127.0.0.1";

	private @Getter @Setter int port = 5432;

	private @Getter @Setter String dbName = "dbName";

	private @Getter @Setter String userName = "userName";

	private @Getter @Setter String userPasswd = "password";

	private @Getter @Setter boolean ssl = false;
}
