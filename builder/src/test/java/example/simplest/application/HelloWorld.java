package example.simplest.application;

/**
 * Example class that does not depend on any other libraries.
 */
public class HelloWorld {

    private static final String GREETING = "Hello World, new here";

    public static void main(String[] args) {
        System.out.println("[example.simplest] " + GREETING);
    }

    public String getGreeting() {
        return HelloWorld.GREETING;
    }
}
