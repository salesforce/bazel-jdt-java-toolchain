public class TestJep280 {
    public static void main(String[] args) {
        String result = "Hello ";
        for (String arg : args) {
            result += arg + ", ";
        }
        System.out.println(result);
    }
}