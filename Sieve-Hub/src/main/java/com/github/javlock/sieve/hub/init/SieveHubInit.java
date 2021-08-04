package com.github.javlock.sieve.hub.init;

import java.io.File;

import com.github.javlock.sieve.hub.SieveHub;

public class SieveHubInit {

	public static void main(String[] args) {
		try {
			SieveHub sieveHub = new SieveHub();

			sieveHub.setDataDir(new File("SieveHubData"));

			sieveHub.setConfigFile(new File(sieveHub.getDataDir(), "SieveHubConfig.yml"));
			sieveHub.setErrorDataDir(new File(sieveHub.getDataDir(), "errorDataDir"));

			sieveHub.init();
			sieveHub.start();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
