package com.vlad.elinext.impls;

import com.vlad.elinext.annotation.Inject;
import com.vlad.elinext.exceptions.BindingNotFoundException;
import com.vlad.elinext.exceptions.ConstructorNotFoundException;
import com.vlad.elinext.exceptions.TooManyConstructorsException;
import com.vlad.elinext.interfaces.Injector;
import com.vlad.elinext.interfaces.Provider;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class InjectorImpl implements Injector {


    // МЕСТО ХРАНЕНИЯ БИНОВ
    private final Map<Class<?>, Bean<?>> beans = new ConcurrentHashMap<>();


    @Override
    public synchronized <T> Provider<T> getProvider(Class<T> type) {

        if (!beans.containsKey(type))
            return null;

        Bean<T> bean = (Bean<T>) beans.get(type);
        T result = bean.getInstance();
        return new ProviderImpl<>(result);
    }


    @Override
    public synchronized <T> void bind(Class<T> intf, Class<? extends T> impl) {
        createBean(intf, impl, BeanPrototype::new);
    }


    @Override
    public synchronized <T> void bindSingleton(Class<T> intf, Class<? extends T> impl) {
        createBean(intf, impl, BeanPrototype::new);
    }


    // СОЗДАНИЕ БИНА
    private <T> void createBean(Class<T> intf, Class<? extends T> impl, function<Bean<?>, T> factory) {

        // ПРОВЕРКА БЫЛ ЛИ ТАКОЙ БИН ДОБАВЛЕН РАНЕЕ
        if (beans.containsKey(intf))
            throw new IllegalArgumentException(String.format("The %s implementation is already installed", intf));

        // ПОИСК И ТЕСТИРОВАНИЕ НУЖНОГО КОНСТРУКТОРА
        List<Constructor<?>> constructors = Arrays.stream(impl.getConstructors())
                .filter(constructor -> constructor.isAnnotationPresent(Inject.class))
                .collect(Collectors.toList());
        if (constructors.size() > 1)
            throw new TooManyConstructorsException(
                    "Должно быть не более одного конструктора отмеченного аннотацией Inject");
        else if (constructors.size() == 0) {
            try {
                Constructor<?> constructor = impl.getConstructor();
                if (!Modifier.isPublic(constructor.getModifiers()))
                    throw new ConstructorNotFoundException(String.format(
                            "Нету такого конструктора, найденного в %s", impl));
                constructors.add(constructor);
            }
            catch (NoSuchMethodException e) {
                throw new ConstructorNotFoundException(String.format(
                        "Нету такого конструктора, найденного в %s", impl));
            }
        }

        // ПРОВЕРКА ПАРАМЕТРОВ КОНСТРУКТОРА
        List<Class<?>> params = Arrays.stream(constructors.get(0)
                .getParameterTypes())
                .filter(key -> !beans.containsKey(key))
                .collect(Collectors.toList());
        if (params.size() != 0) {
            StringBuilder stringBuilder = new StringBuilder("Контейнер не может найти связку с именами");
            stringBuilder.append(params.get(0).getName());
            for (int i = 1; i < params.size(); i++) {
                stringBuilder.append(", ");
                stringBuilder.append(params.get(i));
            }
            throw new BindingNotFoundException(stringBuilder.toString());
        }

        // ДОБАВЛЕНИЕ БИНА В МАПУ
        beans.put(intf, factory.apply(intf, impl, constructors.get(0)));
    }


    // СОЗДАНИЕ ВОЗВРАЩАЕМОГО ОБЪЕКТА
    private Object createObject(final Constructor<?> constructor) {
        Object[] parameters = Arrays.stream(constructor.getParameterTypes()).map(parameter -> {
            Bean<?> bean = beans.get(parameter);
            return bean.getInstance();
        }).toArray();

        // СОЗДАНИЕ НУЖНОГО ОБЪЕКТА
        try {
            return constructor.newInstance(parameters);
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
            throw new IllegalStateException(e.getMessage());
        }
    }


    // ИНТЕРФЕЙС ДЛЯ СОКРАЩЕНИЯ КОДА
    @FunctionalInterface
    private interface function<R, T> {
        R apply(Class<T> intf, Class<? extends T> impl, Constructor<?> constructor);
    }

    private interface Bean<T> {
        T getInstance();
    }


    // РЕАЛИЗАЦИЯ БИНА
    private static abstract class BeanAbstractImpl<T> implements Bean<T> {
        protected final Class<T> intf;
        protected final Class<? extends T> impl;
        protected final Constructor<?> constructor;

        private BeanAbstractImpl(Class<T> intf, Class<? extends T> impl, Constructor<?> constructor) {
            this.intf = intf;
            this.impl = impl;
            this.constructor = constructor;
        }
    }


    // РЕАЛИЗАЦИЯ БИНА (СИНГЛТОН)
    private class BeanSingleton<T> extends BeanAbstractImpl<T> {
        private T object;

        public BeanSingleton(Class<T> intf, Class<? extends T> impl, Constructor<?> constructor) {
            super(intf, impl, constructor);
        }

        @Override
        public T getInstance() {
            if (object != null) return object;
            object = (T) createObject(constructor);
            return object;
        }
    }


    // РЕАЛИЗАЦИЯ БИНА (ПРОТОТИП)
    private class BeanPrototype<T> extends BeanAbstractImpl<T> {

        private BeanPrototype(Class<T> intf, Class<? extends T> impl, Constructor<?> constructor) {
            super(intf, impl, constructor);
        }

        @Override
        public T getInstance() {
            return (T) createObject(constructor);
        }
    }
}
