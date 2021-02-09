package com.vlad.elinext;

import com.vlad.elinext.exceptions.BindingNotFoundException;
import com.vlad.elinext.exceptions.ConstructorNotFoundException;
import com.vlad.elinext.exceptions.TooManyConstructorsException;
import com.vlad.elinext.impls.InjectorImpl;
import com.vlad.elinext.interfaces.Injector;
import com.vlad.elinext.interfaces.Provider;
import com.vlad.elinext.testclasses.impls.*;
import com.vlad.elinext.testclasses.interfaces.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;


public class InjectorTest {

    private Injector injector;

    @BeforeEach
    public void setUp() {
        injector = new InjectorImpl();
    }

    @Test
    void testExistingBinding() {
        injector.bind(A.class, AImpl.class);
        Provider<A> daoProvider = injector.getProvider(A.class);
        assertNotNull(daoProvider);
        assertNotNull(daoProvider.getInstance());
        assertSame(AImpl.class, daoProvider.getInstance().getClass());
    }

    @Test
    void testPrototype() {
        injector.bind(A.class, AImpl.class);
        A a1 = injector.getProvider(A.class).getInstance();
        A a2 = injector.getProvider(A.class).getInstance();
        assertNotEquals(a1, a2);
    }

    @Test
    void testSingleton() {
        injector.bindSingleton(A.class, AImpl.class);
        A a1 = injector.getProvider(A.class).getInstance();
        A a2 = injector.getProvider(A.class).getInstance();
        assertEquals(a1, a2);
    }

    @Test
    void testTooManyConstructorsException() {
        TooManyConstructorsException exception = assertThrows(TooManyConstructorsException.class,
                () -> injector.bind(E.class, EImpl.class));
        assertEquals(exception.getMessage(), "There should be no more than one constructor " +
                "marked with Inject annotation");
    }

    @Test
    void testConstructorNotFoundException() {
        ConstructorNotFoundException exception = assertThrows(ConstructorNotFoundException.class,
                () -> injector.bind(F.class, FImpl.class));
        assertEquals(exception.getMessage(), "No valid constructor found in class FImpl");
    }

    @Test
    void testPrivateConstructor() {
        ConstructorNotFoundException exception = assertThrows(ConstructorNotFoundException.class,
                () -> injector.bind(D.class, DImpl.class));
        assertEquals(exception.getMessage(), "No valid constructor found in class DImpl");
    }

    @Test
    void testBindingNotFoundException() {
        BindingNotFoundException exception = assertThrows(BindingNotFoundException.class,
                () -> injector.bind(B.class, BImpl.class));
        assertEquals(exception.getMessage(), "The container cannot find binding with names: A");
    }

    @Test
    void testNullProvider() {
        assertNull(injector.getProvider(A.class));
    }

    @Test
    void testInheritanceTree() {
        injector.bindSingleton(A.class, AImpl.class);
        injector.bindSingleton(B.class, BImpl.class);
        injector.bindSingleton(C.class, CImpl.class);
        assertEquals(injector.getProvider(B.class).getInstance().getA(),
                injector.getProvider(C.class).getInstance().getA());
    }
}
