package aaa;

import java.io.Serializable;

import lombok.Getter;
import lombok.Setter;

public class Test implements Serializable {
	private @Getter @Setter String data1;
	private @Getter @Setter int data2;

	public Test(String string, int i) {
		setData1(string);
		setData2(i);
	}

	private static final long serialVersionUID = 6263319432856030658L;

}
