package example.simple1.application;

import example.simplest.application.HelloWorld;

public class HelloWithSimpleDependency {

    public static void main(String[] args) {
        HelloWithSimpleDependency app = new HelloWithSimpleDependency();
        System.out.println("[example.simple1] " + app.getGreeting());
    }

    public String getGreeting() {
        HelloWorld helloWorldResource = new HelloWorld();
        return helloWorldResource.getGreeting();
    }

    private void unusedMethod() {
        String unusedLocal = null;
    }
}
