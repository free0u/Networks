import java.util.*;

/**
 * Created by free0u on 9/27/14.
 */
public class Log implements Runnable {
    Map<User, List<Long>> users = new HashMap<>();
    public Log(Map<User, List<Long>> users) {
        this.users = users;
    }

    private void clearConsole() {
        final String ANSI_CLS = "\u001b[2J";
        final String ANSI_HOME = "\u001b[H";
        System.out.print(ANSI_CLS + ANSI_HOME);
        System.out.flush();
    }

    private int getCountMissed(List<Long> timestamps) {
        Long now = (new Date()).getTime();

        while (timestamps.size() > 0) {
            long delta = now - timestamps.get(0);
            if (delta > 20000) { // 20 sec
                timestamps.remove(0);
            } else {
                break;
            }
        }

        int catched = timestamps.size();
        if (catched > 10) return 0;
        return 10 - catched;
    }

    private long getTimeFromLast(List<Long> timestamps) {
        Long now = (new Date()).getTime();
        return now - timestamps.get(timestamps.size() - 1);
    }

    public static String padRight(String s, int n) {
         return String.format("%1$-" + n + "s", s);
    }

    private void printTable() {
        // header
        for (int i = 0; i < 65; i++) {
            System.out.print("-");
        }
        System.out.println();
        System.out.print("| " + padRight("ip", 15) + " ");
        System.out.print("| " + padRight("mac", 17)  + " ");
        System.out.print("| " + padRight("name", 10) + " ");
        System.out.print("| " + padRight("time", 5) + " ");
        System.out.println("| " + padRight("dr", 2) + " |");
        for (int i = 0; i < 65; i++) {
            System.out.print("-");
        }
        System.out.println();


        Map<Integer, List<String>> rows = new TreeMap<>();
        for (int i = 0; i <= 10; i++) {
            rows.put(i, new ArrayList<String>());
        }

        for (Map.Entry<User, List<Long>> entry : users.entrySet()) {
            User user = entry.getKey();
            List<Long> timestamps = entry.getValue();

            if (timestamps.isEmpty()) {
                continue;
            }

            long time = getTimeFromLast(timestamps);
            int missed = getCountMissed(timestamps);

            String row = "";
            row += ("| " + padRight(user.ip, 3 * 4 + 3) + " ");
            row += ("| " + padRight(user.mac, 2 * 6 + 5)  + " ");
            row += ("| " + padRight(user.name, 10) + " ");
            row += ("| " + padRight(String.valueOf(time), 5) + " ");
            row += ("| " + padRight(String.valueOf(missed), 2) + " |");
            rows.get(missed).add(row);
        }
        for (int i = 0; i <= 10; i++) {
            for (String row : rows.get(i)) {
                System.out.println(row);
            }
        }

        for (int i = 0; i < 65; i++) {
            System.out.print("-");
        }
        System.out.println();
    }

    @Override
    public void run() {
        try {
            while (true) {
                clearConsole();
                printTable();
                Thread.sleep(500);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
