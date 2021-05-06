import injector.Injector;

public class Main {
    public static void main(String[] args) {
        Injector injector = new Injector();

        SomeBean bean = new SomeBean();
        injector.inject(bean);

        bean.foo();
    }
}