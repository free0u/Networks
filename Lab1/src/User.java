public class User {
    String ip;
    String mac;
    String name;

    public User(String ip, String mac, String name) {
        this.ip = ip;
        this.mac = mac;
        this.name = name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        User user = (User) o;

        if (ip != null ? !ip.equals(user.ip) : user.ip != null) return false;
        if (mac != null ? !mac.equals(user.mac) : user.mac != null) return false;
        if (name != null ? !name.equals(user.name) : user.name != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = ip != null ? ip.hashCode() : 0;
        result = 31 * result + (mac != null ? mac.hashCode() : 0);
        result = 31 * result + (name != null ? name.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "User{" +
                "ip='" + ip + '\'' +
                ", mac='" + mac + '\'' +
                ", name='" + name + '\'' +
                '}';
    }
}
