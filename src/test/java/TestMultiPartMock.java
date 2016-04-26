import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import com.oreilly.servlet.MultipartRequest;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class TestMultiPartMock {
	private final static  String RESOURSE_DIRECTORY_PATH = "D:\\workspace\\sono\\WebContent\\files\\gallery";
	private final static int SIZE_LIMIT = 50 * 1024 * 1024 ;// 5메가까지 제한 넘어서면 예외발생
	HttpServletRequest mockReq;

	@Before
	public void 처음_처리() {
		mockReq = mock(HttpServletRequest.class);
		when(mockReq.getContentType()).thenReturn("multipart/form-data; boundary=---------------------------3274614561247");
		when(mockReq.getHeader("Content-Type")).thenReturn("multipart/form-data; boundary=---------------------------3274614561247");
	}
	
	@Test
	public void 바운더리_추출_테스트(){
		String boundary = getBoundary(mockReq);
		assertThat(boundary, is("-----------------------------3274614561247"));
	}
	
	@Test
	public void 바이트_테스트(){
		byte[] bytes = "\r".getBytes();
		assertThat(bytes.length, is(1));
	}
	
	@Test
	public void MockServletInputStream_readLine_Test() throws IOException{
		String paramStr = "-----------------------------3274614561247\r\nContent-Disposition: form-data; name=\"record_id\"\r\n123";
		ServletInputStream i = new MockServletInputStream(paramStr.getBytes());
		byte[] buf = new byte[8 * 1024];
		int readLine = i.readLine(buf, 0, buf.length);

		String byteToString = new String(buf,0,readLine);
		//assertEquals("-----------------------------3274614561247\r\n", byteToString);
		assertThat(byteToString, is("-----------------------------3274614561247\r\n"));
	}
	
	@Test
	public void readLine_Test() throws IOException{
		String paramStr = "-----------------------------3274614561247\r\nContent-Disposition: form-data; name=\"record_id\"\r\n123";
		ServletInputStream i = new MockServletInputStream(paramStr.getBytes());
		String line = readLine(i, "utf-8");


		assertThat(line, is("-----------------------------3274614561247"));
	}
	
	@Test
	public void ServletInputStream_Wrapper_만들어주기() throws IOException{
		String paramStr = "-----------------------------3274614561247\r\n"
				+ "Content-Disposition: form-data; name=\"record_id\"\r\n\r\n"

				+ "123\r\n"
				+ "-----------------------------3274614561247\r\n"
				+ "Content-Disposition: form-data; name=\"record_id\"\r\n\r\n"

				+ "123\r\n"
				+ "-----------------------------3274614561247\r\n"
				+ "Content-Disposition: form-data; name=\"contents\"\r\n\r\n"

				+ "안녕하세요\r\n"
				+ "강현구입니다.\r\n\r\n"

				+"-----------------------------3274614561247\r\n"
				+ "Content-Disposition: form-data; name=\"file01\"; filename=\"\"\r\n"
				+ "Content-Type: application/octet-stream\r\n\r\n\r\n"


				+ "-----------------------------3274614561247--\r\n";
		ServletInputStream i = new MockServletInputStream(paramStr.getBytes());
		when(mockReq.getInputStream()).thenReturn(i);
		String boundary = getBoundary(mockReq);
		Map<String, String> params = makeReqeust(mockReq, boundary);

		assertThat(params.get("record_id"), is("123"));
		assertThat(params.get("contents"), is("안녕하세요\n강현구입니다.\n"));
		assertThat(params.get("record_id"), is("123"));
		assertThat(params.get("record_id"), is("123"));
	}

    @Test
    public void 이미지_파일경로_찾기(){
        String rootPath = System.getProperty("user.dir");
        assertThat(rootPath, is("C:\\Users\\khk\\IdeaProjects\\MockServeletInputStream"));

        String path = rootPath + File.separator +"img" + File.separator + "1.jpg";
        assertThat(path, is("C:\\Users\\khk\\IdeaProjects\\MockServeletInputStream\\img\\1.jpg"));
    }
    
    //servletInputStream read는 buffer 에 시작점 부터 끝점까지 담아줌
    @Test
    public void servletInputStream_read_메서드_구현하기(){
        byte[] bytes = new byte[] { 1,2,3,4,5,6,7,8,9,10,11,12,13,14 };
        ServletInputStream i = new MockServletInputStream(bytes);
        byte[] buf = new byte[5];
        try {
            i.read(buf,0,buf.length);
        } catch (IOException e) {
            e.printStackTrace();
        }
        assertThat(buf, is(new byte[]{1,2,3,4,5}));
    }

    //@todo : stream 으로 파일 저장하는 것 테스트
    // 208째 줄 변수 size 49만 번 때 433 번줄 System.arraycopy(this.buf, this.pos, this.buf, 0, this.count - this.pos); 에서 ArrayIndexOutOfBoundsException 발생
    // PartInputStream count 가 1이 많아서 발생하는 것으로 고려중 이나 확실치는 않음
    @Test
	public void stream_으로_파일_저장하는_것_테스트(){
        String rootPath = System.getProperty("user.dir");
        String path = rootPath + File.separator +"img" + File.separator + "1.jpg";
        String savePath = rootPath + File.separator +"img_copy" + File.separator + "1.jpg";
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream( );

        try {
            Path pathForByte = Paths.get(path);
            outputStream.write(Files.readAllBytes(pathForByte));
            outputStream.write("\r\n-----------------------------3274614561247--\r\n".getBytes());
            byte[] byteParts = outputStream.toByteArray( );
            //assertThat(byteParts.length, is(0));
            ServletInputStream i = new MockServletInputStream(byteParts);
            writeTo(new File(savePath),"1.jpg",i,"-----------------------------3274614561247");
        } catch (IOException e) {
            fail(e.getMessage());
        }
	}

	public long writeTo(File fileOrDirectory, String fileName, ServletInputStream in, String boundary) throws IOException {
		long written = 0L;
		BufferedOutputStream fileOut = null;

		try {
			if(fileName != null) {
				File file;
				if(fileOrDirectory.isDirectory()) {
					file = new File(fileOrDirectory, fileName);
				} else {
					file = fileOrDirectory;
				}

				fileOut = new BufferedOutputStream(new FileOutputStream(file));
				written = write(fileOut,in, boundary);
			}
		} finally {
			if(fileOut != null) {
				fileOut.close();
			}

		}

		return written;
	}

	public long write(OutputStream out, ServletInputStream in, String boundary) throws IOException {

		PartInputStream partInput =  new PartInputStream(in, boundary);
		long size = 0L;

		int read;
		for(byte[] buf = new byte[8192]; (read = partInput.read(buf)) != -1; size += (long)read) {
			((OutputStream)out).write(buf, 0, read);
		}

		return size;
	}

	//@todo : 파일 값을 담은 파라미터값 하나 만들어 주기
	//현재 parse 해서 파일로 저장되는 부분이 문제로 파악
    //\r\n 을 한번 더 써서 그런게 아닐까 추측
	@Test
	public void 파일_값을_담은_파라미터값_하나_만들어_주기(){
        String rootPath = System.getProperty("user.dir");
        String path = rootPath + File.separator +"img" + File.separator + "1.jpg";
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream( );
        String frontStr = "-----------------------------3274614561247\r\n"
                + "Content-Disposition: form-data; name=\"file01\"; filename=\"1.jpg\"\r\n"
                + "Content-Type: application/octet-stream\r\n\r\n";

        try {
            outputStream.write(frontStr.getBytes());
            Path pathForByte = Paths.get(path);
            outputStream.write(Files.readAllBytes(pathForByte));
            outputStream.write("\r\n-----------------------------3274614561247--\r\n".getBytes());
            byte byteParts[] = outputStream.toByteArray( );
            ServletInputStream i = new MockServletInputStream(byteParts);
            when(mockReq.getInputStream()).thenReturn(i);
            when(mockReq.getContentLength()).thenReturn(byteParts.length);
            new MultipartRequest(mockReq, rootPath + File.separator +"img_copy", "utf-8");

        } catch (IOException e) {
            fail(e.getMessage());
        }
	}

	public String getBoundary(HttpServletRequest request) {
		String type1 = request.getHeader("Content-Type");
		int index = type1.lastIndexOf("boundary=");

	    String boundary = type1.substring(index + 9);  // 9 for "boundary="
	    if (boundary.charAt(0) == '"') {
	      index = boundary.lastIndexOf('"');
	      boundary = boundary.substring(1, index);
	    }
	    return "--"+boundary;
	}
	
	/**
	 * multiportrequest 에서 파라미터 값을 추출해 맵으로 만들어 리턴한다.
	 * @param request request
	 * @param boundary : 추출될 바운더리
	 */
	public Map<String, String> makeReqeust(HttpServletRequest request, String boundary){
		System.out.println("makeReqeust start ");
		Map<String, String> parameters = new HashMap<String, String>();
		try {
			ServletInputStream in = request.getInputStream();



		  	System.out.println("boundary "+boundary);
			String line = "";
			do {
			      line = readLine(in, "utf-8");
				  System.out.println("find frist line "+line);
			      if (line == null) {
			        throw new IOException("Corrupt form data: premature ending");
			      }
			      // See if this line is the boundary, and if so break
			      if (line.startsWith(boundary)) {
			        break;  // success
			      }
			    } while (true);
				line = readLine(in, "utf-8");
			 while (line!=null
					 && !line.equals(boundary+"--")){
				//head 추출 및 파싱
				//파싱 하는 로직
				String Head2Line = line.toLowerCase() + "; " +readLine(in, "utf-8").toLowerCase();
				System.out.println("Head2Line "+Head2Line);
				String[] tempHead = Head2Line.split(";");
				Map<String, String> header = new HashMap<String, String>();
				for (String head:tempHead) {
					head = head.trim();
					if (!head.equals("")) {
						System.out.println("head "+head);
						String splitChar = "";
						if (head.contains("=\"")) {
							head = head.replaceAll("\"", "");
							splitChar = "=";
						} else if (head.contains(": ")) {
							splitChar = ": ";
						}
						String[] keyAndValue = head.split(splitChar);
						if (keyAndValue.length > 1){
							String key = keyAndValue[0];
							String value = keyAndValue[1];
							header.put(key, value);
						}

					}
				}


				if (header.containsKey("content-type")) {

					/*
					파일로 저정하는 로직
					File file = new File(savePath+File.pathSeparator+"test_img0101.jpg");
					OutputStream fileOut = new BufferedOutputStream(new FileOutputStream(file));

				    long size=0;
				    int read;
				    byte[] buf = new byte[8 * 1024];
				    while((read = in.read(buf)) != -1) {
				    	fileOut.write(buf, 0, read);
				    }*/

					do {
	        			line = readLine(in, "utf-8");
	        			System.out.println("after line "+line);
	        		} while (!line.contains(boundary));
				} else {
					do {
						line = readLine(in, "utf-8");
					} while (line.trim().equals(""));
					//여러행으로 이루어진 value 값을 파싱하기 위해 for 문을 돌림
					do {
						String temp = readLine(in, "utf-8");
						if (temp.startsWith(boundary)) break;
						line += "\n"+temp;
					} while (true);
					String val = line;
					parameters.put(header.get("name"), val);
					line = readLine(in, "utf-8");
				}

			 }

		} catch (IOException e ){
			e.printStackTrace();
		} catch (NullPointerException e) {
            e.printStackTrace();
        }
		return parameters;
	}
	
	/**
	 * MultiPartRequest 에 소스에서 추출한 메서드 내가 조금 수정하였다.
	 * @param in
	 * @param encoding
	 * @return
	 * @throws IOException
	 */
	private String readLine(ServletInputStream in, String encoding) throws IOException {
	    StringBuffer sbuf = new StringBuffer();
	    int result;
	    String line;
	    byte[] buf = new byte[8 * 1024];

	    do {
	      result = in.readLine(buf, 0, buf.length);  // does +=
	      if (result != -1) {
	        sbuf.append(new String(buf, 0, result, encoding));
	      }
	    } while (result == buf.length);  // loop only if the buffer was filled

	    if (sbuf.length() == 0) {
	      return null;  // nothing read, must be at the end of stream
	    }

	    // Cut off the trailing \n or \r\n
	    // It should always be \r\n but IE5 sometimes does just \n
	    // Thanks to Luke Blaikie for helping make this work with \n
	    int len = sbuf.length();
	    if (len >= 2 && sbuf.charAt(len - 2) == '\r') {
	      sbuf.setLength(len - 2);  // cut \r\n
	    }
	    else if (len >= 1 && sbuf.charAt(len - 1) == '\n') {
	      sbuf.setLength(len - 1);  // cut \n
	    }
	    return sbuf.toString();
	  }

	@After
	public void 마지막_처리(){
	}
	
	//@todo : 파라미터 값을 MultiPartRequest 라이브러리로 추출할 수 있어야함
	//@todo : 파라미터 값을 FileUpload 라이브러리로 추출할 수 있어야함
	/*
	 * try {
			new MultipartRequest(mockReq, RESOURSE_DIRECTORY_PATH, SIZE_LIMIT);
		} catch (IOException e) {
			e.printStackTrace();
		}
	 * */
	
	public class MockServletInputStream extends ServletInputStream {
		byte[] store;
		int pos;

		public MockServletInputStream(byte[] bytes) {
			store = bytes;
			pos = 0;
		}

		@Override
		public int read() throws IOException {
			return pos+1 >= store.length ? -1 : (int)store[pos++];
		}

		@Override
		public int readLine(byte[] b, int off, int len) throws IOException {
			int read = Math.min(store.length - pos, len);
			int i = -1;
			while (++i < read ) {
				b[off+i] = store[pos+i];
				if(store[pos+i]=="\r".getBytes()[0]){
					if(store[pos+i+1]=="\n".getBytes()[0]){
						i++;
						b[off+i] = store[pos+i];
						break;
					}
				}
			}
			pos += ++i;
			return i;
		}
	}
	

}


class PartInputStream extends FilterInputStream {
	private String boundary;
	private byte[] buf = new byte[65536];
	private int count;
	private int pos;
	private boolean eof;

	PartInputStream(ServletInputStream in, String boundary) throws IOException {
		super(in);
		this.boundary = boundary;
	}

	private void fill() throws IOException {
		if(!this.eof) {
			if(this.count > 0) {
				if(this.count - this.pos != 2) {
					throw new IllegalStateException("fill() detected illegal buffer state");
				}

				System.arraycopy(this.buf, this.pos, this.buf, 0, this.count - this.pos);
				this.count -= this.pos;
				this.pos = 0;
			}

			boolean read = false;
			int boundaryLength = this.boundary.length();

			int var5;
			for(int maxRead = this.buf.length - boundaryLength - 2; this.count < maxRead; this.count += var5) {
				var5 = ((ServletInputStream)super.in).readLine(this.buf, this.count, this.buf.length - this.count);
				if(var5 == -1) {
					throw new IOException("unexpected end of part");
				}

				if(var5 >= boundaryLength) {
					this.eof = true;

					for(int i = 0; i < boundaryLength; ++i) {
						if(this.boundary.charAt(i) != this.buf[this.count + i]) {
							this.eof = false;
							break;
						}
					}

					if(this.eof) {
						break;
					}
				}
			}

		}
	}

	public int read() throws IOException {
		if(this.count - this.pos <= 2) {
			this.fill();
			if(this.count - this.pos <= 2) {
				return -1;
			}
		}

		return this.buf[this.pos++] & 255;
	}

	public int read(byte[] b) throws IOException {
		return this.read(b, 0, b.length);
	}

	public int read(byte[] b, int off, int len) throws IOException {
		byte total = 0;
		if(len == 0) {
			return 0;
		} else {
			int avail = this.count - this.pos - 2;
			if(avail <= 0) {
				this.fill();
				avail = this.count - this.pos - 2;
				if(avail <= 0) {
					return -1;
				}
			}

			int copy = Math.min(len, avail);
			System.arraycopy(this.buf, this.pos, b, off, copy);
			this.pos += copy;

			int total1;
			for(total1 = total + copy; total1 < len; total1 += copy) {
				this.fill();
				avail = this.count - this.pos - 2;
				if(avail <= 0) {
					return total1;
				}

				copy = Math.min(len - total1, avail);
				System.arraycopy(this.buf, this.pos, b, off + total1, copy);
				this.pos += copy;
			}

			return total1;
		}
	}

	public int available() throws IOException {
		int avail = this.count - this.pos - 2 + super.in.available();
		return avail < 0?0:avail;
	}

	public void close() throws IOException {
		if(!this.eof) {
			while(this.read(this.buf, 0, this.buf.length) != -1) {
				;
			}
		}

	}
}