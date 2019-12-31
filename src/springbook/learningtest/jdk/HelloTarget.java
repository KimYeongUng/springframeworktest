package springbook.learningtest.jdk;

public class HelloTarget implements Hello {
    @Override
    public String SayHello(String name) {
        return "Hello "+name;
    }

    @Override
    public String SayHi(String name) {
        return "Hi "+name;
    }

    @Override
    public String SayThankYou(String name) {
        return "Thank You "+name;
    }
}
