
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class Main {
    public static void main(String[] args) {
        System.out.println("[" + args[0] + "]" + {call});
    }

    public static int[] asIntArray(String arg) {
        return Arrays.stream(arg.substring(1, arg.length() - 1).split(",\\s*")).mapToInt(Integer::parseInt)
                .toArray();
    }

    public static int asInt(String arg) {
        return Integer.parseInt(arg);
    }

    public static String toString(int[] arg){
        return Arrays.toString(arg);
    }
}

{snippet}
