package injector;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.Properties;

public class Injector {
    private static final String PROPERTIES_FILE = "inj.properties";

    private final Properties props;

    /**
     * Конструктор, который загружает свойства из данного файла
     * @param propertiesFileName имя файла свойств
     */
    public Injector(String propertiesFileName){
        props = new Properties();

        InputStream propsInputStream = getClass().getClassLoader().getResourceAsStream(propertiesFileName);
        if (propsInputStream == null) {
            throw new RuntimeException("Не удалось загрузить файл inj.properties");
        }

        try {
            props.load(propsInputStream);
        } catch (IOException exception){
            throw new RuntimeException("Не удалось загрузить файл inj.properties");
        }
    }

    /**
     * Стандартный конструктор. Загружает конфигурацию из инжектора.свойства
     */
    public Injector(){
        this(PROPERTIES_FILE);
    }

    /**
     * Ввод полей с аннотацией @AutoInjectable
     * @param bean Object to inject fields in
     * @param <T> type of object
     */
    public <T> void inject(T bean){
        Class<?> beanClass = bean.getClass();

        Field[] fields = beanClass.getDeclaredFields();

        for (Field field : fields){
            if (field.isAnnotationPresent(AutoInjectable.class)){
                Class<?> fieldClass = field.getType();
                Class<?> implementationClass = findImplementationClass(fieldClass);

                if (implementationClass == null){
                    throw new RuntimeException("Не удалось найти реализацию для " + fieldClass.getName());
                }

                Object implementation = instantiateClassEmptyConstructor(implementationClass);

                field.setAccessible(true);
                try {
                    field.set(bean, implementation);
                } catch (Exception ex){
                    throw new RuntimeException("Невозможно ввести поле " + field.getName());
                }
            }
        }
    }

    /**
     * Находит класс реализации для данного интерфейса
     * Если {@param interfaceClass} не является интерфейсом, вызывает исключение RuntimeException
     * @param interfaceClass класс, для которого выполняется поиск класса реализации
     * @return class from props that implements {@param interfaceClass}
     */
    private Class<?> findImplementationClass(Class<?> interfaceClass){
        if (!interfaceClass.isInterface()){
            throw new RuntimeException("Нельзя вводить поля, которые не являются интерфейсом");
        }

        String implementationClassName = props.getProperty(interfaceClass.getName());
        if (implementationClassName == null){
            return null;
        }

        Class<?> implementationClass = null;
        try {
            implementationClass = this.getClass().getClassLoader().loadClass(implementationClassName);
        } catch (Exception ex){
            throw new RuntimeException("Не удалось загрузить класс " + implementationClassName);
        }

        return implementationClass;
    }

    /**
     * Создает экземпляр класса с пустым конструктором, если он существует, в противном случае вызывает исключение RuntimeException
     * @param clazz класс для создания экземпляра
     * @param <T> тип экземпляра объекта
     * @return объект, созданный с помощью пустого конструктора
     */
    private <T> T instantiateClassEmptyConstructor(Class<T> clazz){
        Constructor<T> emptyConstructor = null;
        try {
            emptyConstructor = clazz.getConstructor();
        } catch (Exception ex){
            throw new RuntimeException("Не удается создать экземпляр класса " + clazz.getName() + " (пустой конструктор не найден)");
        }

        try {
            return emptyConstructor.newInstance();
        } catch (Exception ex){
            throw new RuntimeException("Не удалось создать экземпляр класса" + clazz.getName());
        }
    }

}
