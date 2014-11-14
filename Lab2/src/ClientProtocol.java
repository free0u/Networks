import java.io.*;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ClientProtocol {
    private String ipToString(byte[] ip) {
        String res = "";
        for (int i = 0; i < 4; ++i) {
            res += (ip[i] & 0xFF);
            if (i < 3) {
                res += ".";
            }
        }
        return res;
    }

    private byte[] getMD5(File file) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            Path path = Paths.get(file.getAbsolutePath());
            byte[] t = Files.readAllBytes(path);
            return md.digest(t);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    final private static char[] hexArray = "0123456789abcdef".toCharArray();
    private static String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for ( int j = 0; j < bytes.length; j++ ) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }

    private byte[] concat(byte[] first, byte[] second) {
        byte[] result = Arrays.copyOf(first, first.length + second.length);
        System.arraycopy(second, 0, result, first.length, second.length);
        return result;
    }

    public Host parseHost(byte[] data) throws IOException {
        byte[] rawIp = Arrays.copyOfRange(data, 0, 4);
        byte[] rawCount = Arrays.copyOfRange(data, 4, 8);
        byte[] rawTimestamp = Arrays.copyOfRange(data, 8, 16);
        byte[] rawName = null;

        for (int i = 16; i < data.length; i++) {
            if (data[i] == 0) {
                rawName = Arrays.copyOfRange(data, 16, i);
                break;
            }
        }

        // ip
        String ip = ipToString(rawIp);

        // file count
        ByteBuffer buf = ByteBuffer.wrap(rawCount);
        int fileCount = buf.getInt();

        // timestamp
        buf = ByteBuffer.wrap(rawTimestamp);
        long timestamp = buf.getLong();

        // name
        String name = null;
        try {
            name = new String(rawName, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            name = "error_name";
        }

        return new Host(ip, fileCount, timestamp, name);
    }

    public List<FileEntry> parseList(InputStream in) throws IOException {
        // [1, 0, 0, 0, 1, -44, 29, -116, -39, -113, 0, -78, 4, -23, -128, 9, -104, -20, -8, 66, 126, 97, 0]


        List<FileEntry> res = new ArrayList<>();

        Parser parser = new Parser(in, 1024);

        // code
        byte[] code = parser.get(1);
        if (code[0] != 0x4) {
            throw new IOException("invalid code of answer");
        }

        // count
        byte[] rawCount = parser.get(4);
        ByteBuffer buf = ByteBuffer.wrap(rawCount);
        int fileCount = buf.getInt();

        for (int i = 0; i < fileCount; i++) {
            byte[] rawFileInfo = parser.getToZeroWithSkip(16);
            if (rawFileInfo == null) {
                continue;
            }

            byte[] rawMD5 = Arrays.copyOfRange(rawFileInfo, 0, 16);
            byte[] rawName = Arrays.copyOfRange(rawFileInfo, 16, rawFileInfo.length - 1);

            String name = null;
            try {
                name = new String(rawName, "UTF-8");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
                name = "error_name";
            }

            String md5 = bytesToHex(rawMD5);
            res.add(new FileEntry(name, md5));
        }

        return res;
    }

    public boolean parseGet(InputStream in, String filename) throws IOException {
        Parser parser = new Parser(in, 1024);

        // code
        byte[] code = parser.get(1);
        if (code[0] != 0x5) {
            throw new IOException("invalid code of answer");
        }


        byte[] rawSize = parser.get(8);
        byte[] rawMD5 = parser.get(16);

        ByteBuffer buf = ByteBuffer.wrap(rawSize);
        long fileSize = buf.getLong();

        String path = "./Tmp/" + filename;
        FileOutputStream fos = new FileOutputStream(path);

        long writed = 0;
        while (writed < fileSize) {
            byte[] t = parser.get(1024);
            writed += t.length;
            fos.write(t);
            if (t.length < 1024) {
                break;
            }
        }
        fos.close();

        byte[] newMD5 = getMD5(new File(path));
        for (int i = 0; i < 16; i++ ) {
            if (rawMD5[i] != newMD5[i]) {
                (new File(path)).delete();
                return false;
            }
        }

        Files.move(Paths.get(path), Paths.get("./Share/" + filename), StandardCopyOption.REPLACE_EXISTING);

        return true;
    }

    public byte[] list() {
        return new byte[]{0x1};
    }

    public byte[] get(String name) {
        byte[] code = new byte[]{0x2};

        byte[] byteName = null;
        try {
            byteName = name.getBytes("UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        byte[] res = new byte[0];
        res = concat(res, code);
        res = concat(res, byteName);
        res = concat(res, new byte[]{0});

        return res;
    }

    public void put(OutputStream out, String name) throws IOException {
        // code
        byte[] code = new byte[]{0x3};

        // name
        byte[] byteName = null;
        try {
            byteName = name.getBytes("UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        // size
        File file = new File("./Share/" + name);
        long fileSize = file.length();
        ByteBuffer bufSize = ByteBuffer.allocate(8);
        bufSize.putLong(fileSize);
        byte[] rawSize = bufSize.array();

        out.write(code);
        out.write(byteName);
        out.write(new byte[]{0});
        out.write(rawSize);

        FileInputStream fis = new FileInputStream(file);
        byte[] buf = new byte[1024];
        int c = fis.read(buf);
        while (c != -1) {
            out.write(buf, 0, c);
            c = fis.read(buf);
        }
        System.out.println("put complete");
    }
}
