import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

public class Parser {
    InputStream in;
    int bufLen;
    byte[] buf;
    int total;

    public Parser(InputStream in, int bufLen) {
        this.in = in;
        this.bufLen = bufLen;
        buf = new byte[bufLen];
        total = 0;
    }

    public byte[] get(int n) throws IOException {
        if (n > bufLen) {
            throw new IOException("n must be lower or equal that bufLen");
        }

        while (n > total) {
            int c = in.read(buf, total, bufLen - total);
            if (c == -1) {
                return Arrays.copyOf(buf, total);
            } else {
                total += c;
            }
        }

        byte[] res = Arrays.copyOf(buf, n);
        for (int i = n; i < total ; i++) {
            buf[i - n] = buf[i];
        }
        total -= n;

        return res;
    }

    private int findZero(int skip) {
        for (int i = skip; i < total; i++) {
            if (buf[i] == 0) return i;
        }
        return -1;
    }


    public byte[] getToZeroWithSkip(int skip) throws IOException {
        int pos = findZero(skip);
        while (pos == -1) {
            if (total == bufLen) {
                return null;
            }

            int c = in.read(buf, total, bufLen - total);
            if (c == -1) {
                return null;
            }
            total += c;
            pos = findZero(skip);
        }

        byte[] res = Arrays.copyOf(buf, pos + 1);
        for (int i = pos + 1; i < total; i++) {
            buf[i - pos - 1] = buf[i];
        }
        total -= (pos + 1);
        return res;
    }

    public byte[] getToZero() throws IOException {
        return getToZeroWithSkip(0);
    }
}
