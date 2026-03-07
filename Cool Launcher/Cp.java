
import java.io.*;
import java.util.*;

public class Cp {
    public static void solve(int n, int[] a) {
        Set<Integer> set = new HashSet<>();
        for (int num : a) {
            set.add(num);
        }
        for (int i = 1; i <= 1000000000; i++) {
            if (!set.contains(i)) {
                System.out.println(i);
                return;
            }
        }
        System.out.println(-1);
    }

    public static void main(String[] args) {
        Scanner read = new Scanner(System.in);
        if (System.getProperty("ONLINE_JUDGE") == null) {
            try {
                System.setOut(new PrintStream(
                        new FileOutputStream("output.txt")));
                read = new Scanner(new File("input.txt"));
            } catch (FileNotFoundException e) {
            }
        }

        int t = read.nextInt();
        while (t-- > 0) {
            int n = read.nextInt();
            int[] a = new int[n];

            for (int i = 0; i < n; i++) {
                int x = read.nextInt();
                a[i] = x;
            }
            solve(n, a);
        }
        read.close();
    }

}
