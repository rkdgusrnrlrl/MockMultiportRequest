import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.junit.Before;
import org.junit.Test;

import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import java.io.*;
import java.util.Iterator;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class TestCommonFileUpload {
	public static final String ROOT_PATH = System.getProperty("user.dir");
	public static final String FILE_PATH = ROOT_PATH + File.separator + "img" + File.separator + "1.jpg";
	public static final String PARAM_STR = "-----------------------------3274614561247\r\n"
			+ "Content-Disposition: form-data; name=\"record_id\"\r\n\r\n"

			+ "123\r\n"
			+ "-----------------------------3274614561247\r\n"
			+ "Content-Disposition: form-data; name=\"record_id\"\r\n\r\n"

			+ "123\r\n"
			+ "-----------------------------3274614561247\r\n"
			+ "Content-Disposition: form-data; name=\"contents\"\r\n\r\n"

			+ "안녕하세요\r\n"
			+ "강현구입니다.\r\n\r\n"

			+ "-----------------------------3274614561247\r\n"
			+ "Content-Disposition: form-data; name=\"file01\"; filename=\"\"\r\n"
			+ "Content-Type: application/octet-stream\r\n\r\n\r\n"


			+ "-----------------------------3274614561247--\r\n";
	public static final String[] PARAM_NAMES = new String[]{"record_no", "title", "contents"};
	public static final String[] VALUES = new String[]{"00001", "제목입니다.", "안녕하세요\n내용부입니다."};
	public static final String PARAM_NAME_FOR_FILE = "up_file";
	public static final String PARAM_NAME = "record_no";
	public static final String VALUE = "00001";
	private final static String RESOURSE_DIRECTORY_PATH = "D:\\workspace\\sono\\WebContent\\files\\gallery";
	private final static int SIZE_LIMIT = 50 * 1024 * 1024;// 5메가까지 제한 넘어서면 예외발생
	HttpServletRequest mockReq;

	@Before
	public void 처음_처리() {
		mockReq = mock(HttpServletRequest.class);
		when(mockReq.getContentType()).thenReturn("multipart/form-data; boundary=---------------------------3274614561247");
		when(mockReq.getHeader("Content-Type")).thenReturn("multipart/form-data; boundary=---------------------------3274614561247");
	}


	@Test
	public void Part_에_파리미터_및_파일을_담아주기() {

		byte[] b = null;
		try {
			Parts parts = new Parts();
			addParamParts(parts);
			parts.addFilePart(PARAM_NAME_FOR_FILE, FILE_PATH);
			b = parts.makeByte();
		} catch (IOException e) {
			e.printStackTrace();
		}

		ServletInputStream in = new MockServletInputStream(b);
		try {
			when(mockReq.getInputStream()).thenReturn(in);
			when(mockReq.getContentLength()).thenReturn(b.length);


			DiskFileItemFactory factory = new DiskFileItemFactory();
			File repository = new File(ROOT_PATH + File.separator + "img_copy");
			factory.setRepository(repository);
			ServletFileUpload upload = new ServletFileUpload(factory);

			// Parse the request
			try {
				List<FileItem> items = upload.parseRequest(mockReq);
				Iterator<FileItem> iterator = items.iterator();
				while (iterator.hasNext()) {
					FileItem item = iterator.next();
					if (item.isFormField()) {

					} else {
						File file = new File(ROOT_PATH + File.separator + "img_copy" + File.separator + "1.jpg");
						saveItemToFile(item, file);
					}
				}
			} catch (FileUploadException e) {
				fail();
			}
		} catch (IOException e) {
			fail();
		}
	}

	public void saveItemToFile(FileItem item, File file) throws IOException {
		InputStream inputStream = item.getInputStream();
		OutputStream outStream = new FileOutputStream(file);

		byte[] buf = new byte[1024];
		int len = 0;

		while ((len = inputStream.read(buf)) > 0) {
            outStream.write(buf, 0, len);
        }

		outStream.close();
		inputStream.close();
	}

	public void addParamParts(Parts parts) {
		for (int i = 0; i < PARAM_NAMES.length; i++) {
			parts.addPart(PARAM_NAMES[i], VALUES[i]);
		}
	}

}





