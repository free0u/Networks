public class Host {
    String ip;
    int fileCount;
    long timestamp;
    String name;

    public Host(String ip, int fileCount, long timestamp, String name) {
        this.ip = ip;
        this.fileCount = fileCount;
        this.timestamp = timestamp;
        this.name = name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Host host = (Host) o;

        if (fileCount != host.fileCount) return false;
        if (timestamp != host.timestamp) return false;
        if (!ip.equals(host.ip)) return false;
        if (!name.equals(host.name)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = ip.hashCode();
        result = 31 * result + fileCount;
        result = 31 * result + (int) (timestamp ^ (timestamp >>> 32));
        result = 31 * result + name.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "Host{" +
                "ip='" + ip + '\'' +
                ", fileCount=" + fileCount +
                ", timestamp=" + timestamp +
                ", name='" + name + '\'' +
                '}';
    }
}
