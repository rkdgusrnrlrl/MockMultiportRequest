import javax.servlet.ServletInputStream;
import java.io.IOException;

/**
 * Created by khk on 2016-05-25.
 */
public class MockServletInputStream extends ServletInputStream {
    byte[] store;
    int pos;

    public MockServletInputStream(byte[] bytes) {
        store = bytes;
        pos = 0;
    }

    private int toInt(byte b) {
        return (-(b - 127) / 128) * 256 + b;
    }

    //read는 byte 값을 int 변형 하여 리턴함
    //즉 음수의 경우 양수인 int 값으로 바뀜
    //int -> byte => 0 -> 0 ... 127 -> 127, 128 -> -128 ... 255 = -1
    @Override
    public int read() throws IOException {
        if (pos >= store.length) {
            return -1;
        } else {
            return toInt(store[pos++]);
        }
    }


}
