import org.junit.*;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

public class TestLearnByte {

    @Before
	public void setUp() {
	}
	
	@org.junit.Test
	public void sample(){
		fail();
	}

	@org.junit.Test
	public void 바이트_테스트(){
		byte[] bytes = "\r".getBytes();
		assertThat(bytes.length, is(1));
	}

    @After
	public void tear(){
	}
}


