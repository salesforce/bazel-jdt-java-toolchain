package example.srcjartest.application;

import example.srcjartest.external.Greeting;

/**
 * Example class that depends on code from a source jar.
 */
public class HelloWorldSrcJar {

    private static final Greeting externalGreeting = new Greeting();

    public static void main(String[] args) {
        System.out.println("[example.srcjartest] " + externalGreeting.getGreeting());
    }
}
