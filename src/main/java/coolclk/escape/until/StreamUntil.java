package coolclk.escape.until;

import com.google.gson.JsonObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

public class StreamUntil {
    public static void transform(InputStream from, OutputStream to) throws IOException {
        if (from != null && to != null) {
            to.write(readInputStreamAllBytes(from));
            from.close();
            to.flush();
            to.close();
        }
    }

    public static byte[] readInputStreamAllBytes(InputStream is) throws IOException {
        if (is != null) {
            int i = 0, l = is.available(), b;
            byte[] bytes = new byte[l];
            while (i < l && (b = is.read()) != -1) {
                bytes[i] = (byte) b;
                i++;
            }
            is.close();
            return bytes;
        }
        return new byte[0];
    }
    public static void writeJSONOutputStream(OutputStream os, JsonObject json) throws IOException {
        if (os != null && json != null) {
            os.write(json.toString().getBytes(StandardCharsets.UTF_8));
            os.close();
        }
    }
}
