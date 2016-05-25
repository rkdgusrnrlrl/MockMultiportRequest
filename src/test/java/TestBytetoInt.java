import org.junit.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class TestBytetoInt {

	/**
	 * InputStream 에서 byte를 int 로 바꿔서 리턴하는데
	 * 음수의 경우 양수인 int 값으로 바뀜
	 * int -> byte => 0 -> 0 ... 127 -> 127, 128 -> -128 ... 255 = -1
	 * @param b
	 * @return
     */
	public int toInt(byte b) {
		return (-(b-127)/128)*256+b;
	}
	//int -> byte => 0 -> 0 ... 127 -> 127, 128 -> -128 ... 255 = -1
	@org.junit.Test
	public void read_함수를_위한_로직_테스트(){
		byte b = 0;
		assertEquals(0, toInt(b));
		b = 127;
		assertEquals(127, toInt(b));
		b = -128;
		assertEquals(128, toInt(b));
		b = -127;
		assertEquals(129, toInt(b));
		b = -1;
		assertEquals(255, toInt(b));
	}

}


