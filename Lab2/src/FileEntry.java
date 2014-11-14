public class FileEntry {
    String name;
    String md5;

    public FileEntry(String name, String md5) {
        this.name = name;
        this.md5 = md5;
    }

    @Override
    public String toString() {
        return "FileEntry{" +
                "name='" + name + '\'' +
                ", md5='" + md5 + '\'' +
                '}';
    }
}
