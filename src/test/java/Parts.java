import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by khk on 2016-05-25.
 */
class Parts {
    private ArrayList<Map<String, Object>> parts;

    public Parts() {
        parts = new ArrayList<Map<String, Object>>();
    }

    public void addPart(String paramName, String value) {
        Map<String, Object> part = new HashMap<String, Object>();
        part.put("name", paramName);
        part.put("type", "string");
        part.put("value", value);
        parts.add(part);
    }

    public void addFilePart(String paramNameForFile, String filePath) {
        Map<String, Object> part = new HashMap<String, Object>();
        part.put("name", paramNameForFile);
        part.put("fileName", "1.jpg");
        part.put("type", "file");
        part.put("value", filePath);
        parts.add(part);
    }

    public byte[] makeByte() throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        int size = parts.size();
        for (Map<String, Object> part : parts) {
            if (part.get("type").equals("string")) {
                String paramStr =
                        "-----------------------------3274614561247\r\n"
                                + "Content-Disposition: form-data; name=\"" + (String) part.get("name") + "\"\r\n\r\n"
                                + (String) part.get("value") + "\r\n";
                outputStream.write(paramStr.getBytes());
            } else if (part.get("type").equals("file")) {
                String frontStr = "-----------------------------3274614561247\r\n"
                        + "Content-Disposition: form-data; name=\"" + (String) part.get("name") + "\"; "
                        + "filename=\"" + (String) part.get("fileName") + "\"\r\n"
                        + "Content-Type: application/octet-stream\r\n\r\n";


                outputStream.write(frontStr.getBytes());
                Path pathForByte = Paths.get((String) part.get("value"));
                outputStream.write(Files.readAllBytes(pathForByte));
            }

            if (--size == 0) {
                outputStream.write("\r\n-----------------------------3274614561247--\r\n".getBytes());
            }

        }
        return outputStream.toByteArray();
    }


}
