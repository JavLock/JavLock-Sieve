package com.github.javlock.sieve.operator.init;

import java.io.File;

import com.github.javlock.sieve.operator.SieveOperator;

public class SieveOperatorInit {

	public static void main(String[] args) {
		try {
			SieveOperator sieveOperator = new SieveOperator();
			sieveOperator.setDataDir(new File("SieveOperatorData"));
			sieveOperator.setConfigFile(new File(sieveOperator.getDataDir(), "SieveOperatorConfig.yml"));

			sieveOperator.init();
			sieveOperator.start();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
