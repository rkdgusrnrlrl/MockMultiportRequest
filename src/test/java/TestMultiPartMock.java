import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
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
			while (i++ < read ) {
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

	HttpServletRequest mockReq;
	private final static  String RESOURSE_DIRECTORY_PATH = "D:\\workspace\\sono\\WebContent\\files\\gallery";
	private final static int SIZE_LIMIT = 50 * 1024 * 1024 ;// 5메가까지 제한 넘어서면 예외발생
	
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

	//@todo : 파일 값을 담은 파라미터값 하나 만들어 주기
	@Test
	public void 파일_값을_담은_파라미터값_하나_만들어_주기(){
        String rootPath = System.getProperty("user.dir");
        String path = rootPath + File.separator +"img" + File.separator + "1.jpg";
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream( );
        String frontStr = "-----------------------------3274614561247\r\n"
                + "Content-Disposition: form-data; name=\"file01\"; filename=\"\"\r\n"
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
	
	//@todo : 파라미터 값을 MultiPartRequest 라이브러리로 추출할 수 있어야함
	//@todo : 파라미터 값을 FileUpload 라이브러리로 추출할 수 있어야함
	/*
	 * try {
			new MultipartRequest(mockReq, RESOURSE_DIRECTORY_PATH, SIZE_LIMIT);
		} catch (IOException e) {
			e.printStackTrace();
		}
	 * */
	
	
	@After
	public void 마지막_처리(){
	}
	

}
