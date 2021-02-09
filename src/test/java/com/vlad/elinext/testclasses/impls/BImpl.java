package com.vlad.elinext.testclasses.impls;

import com.vlad.elinext.annotation.Inject;
import com.vlad.elinext.testclasses.interfaces.A;
import com.vlad.elinext.testclasses.interfaces.B;

public class BImpl implements B {
    private final A a;

    @Inject
    public BImpl(A a) {
        this.a = a;
    }

    @Override
    public A getA() {
        return a;
    }
}
