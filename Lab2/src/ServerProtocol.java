import java.io.*;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;

public class ServerProtocol {
    private ArrayList<File> listFiles() {

        File folder = new File("./Share");
        File[] files = folder.listFiles();

        return new ArrayList<>(Arrays.asList(files));
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

    public byte[] announce() throws SocketException, UnknownHostException {
        // ip
        InetAddress localHost = InetAddress.getLocalHost();
        byte[] ip = localHost.getAddress();

        // file count
        int count = listFiles().size();

        // timestamp modified
        long modified = (new File("./Share")).lastModified();

        // name
        String name = "Evdokimov";
        byte[] byteName = null;
        try {
            byteName = name.getBytes("UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        ByteBuffer bufCount = ByteBuffer.allocate(4);
        ByteBuffer bufMod = ByteBuffer.allocate(8);
        bufCount.putInt(count);
        bufMod.putLong(modified);

        byte[] res = new byte[ip.length + bufCount.array().length + bufMod.array().length + byteName.length + 1];
        int ind = 0;
        for (byte i : ip) {
            res[ind++] = i;
        }
        for (byte i : bufCount.array()) {
            res[ind++] = i;
        }
        for (byte i : bufMod.array()) {
            res[ind++] = i;
        }
        for (byte i : byteName) {
            res[ind++] = i;
        }
        res[ind] = 0;

        return res;
    }


    public byte[] list() {
        // code
        byte[] code = new byte[1];
        code[0] = 0x4;

        ArrayList<File> files = listFiles();

        // file count
        ByteBuffer bufCount = ByteBuffer.allocate(4);
        bufCount.putInt(files.size());
        byte[] fileCount = bufCount.array();

        byte[] filesEncoded = new byte[0];
        for (File file : files) {
            byte[] md5 = getMD5(file);

            byte[] byteName = null;
            try {
                byteName = file.getName().getBytes("UTF-8");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }

            byte[] fileEncoded = new byte[0];
            fileEncoded = concat(fileEncoded, md5);
            fileEncoded = concat(fileEncoded, byteName);
            fileEncoded = concat(fileEncoded, new byte[]{0});

            filesEncoded = concat(filesEncoded, fileEncoded);
        }

        byte[] res = new byte[0];
        res = concat(res, code);
        res = concat(res, fileCount);
        res = concat(res, filesEncoded);



        return res;
    }

    public void get(String name, OutputStream out) throws IOException {
        File file = new File("./Share/" + name);

        // code
        byte[] code = new byte[]{0x5};

        // size
        long fileSize = file.length();
        ByteBuffer bufSize = ByteBuffer.allocate(8);
        bufSize.putLong(fileSize);
        byte[] rawSize = bufSize.array();

        // md5
        byte[] md5 = getMD5(file);

        out.write(code);
        out.write(rawSize);
        out.write(md5);

        byte[] buf = new byte[1024];
        FileInputStream fis = new FileInputStream(file);
        int c = fis.read(buf);
        while (c != -1) {
            out.write(buf, 0, c);
            c = fis.read(buf);
        }
    }

    public void parsePut(Parser parser) throws IOException {
        byte[] rawName = parser.getToZero();
        byte[] rawSize = parser.get(8);


        ByteBuffer buf = ByteBuffer.wrap(rawSize);
        long fileSize = buf.getLong();

        try {
            String name = new String(Arrays.copyOf(rawName, rawName.length - 1), "UTF-8");

            String path = "./Tmp/" + name;
            FileOutputStream fos = new FileOutputStream(path);

            long writed = 0;
            while (writed < fileSize) {
                long estimate = fileSize - writed;
                int len = -1;
                if (estimate > 1024) {
                    len = 1024;
                } else {
                    len = (int) estimate;
                }

                byte[] t = parser.get(len);
                writed += t.length;
                fos.write(t);
                if (t.length < 1024) {
                    break;
                }
            }
            fos.close();
            if (writed < fileSize) {
                (new File(path)).delete();
            }
            System.out.println("move");
            Files.move(Paths.get(path), Paths.get("./Share/" + name), StandardCopyOption.REPLACE_EXISTING);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }
}
