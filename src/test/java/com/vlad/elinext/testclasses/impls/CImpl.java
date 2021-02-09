package com.vlad.elinext.testclasses.impls;

import com.vlad.elinext.annotation.Inject;
import com.vlad.elinext.testclasses.interfaces.A;
import com.vlad.elinext.testclasses.interfaces.C;

public class CImpl implements C {
    private final A a ;

    @Inject
    public CImpl(A a) {
        this.a = a;
    }

    @Override
    public A getA() {
        return a;
    }
}
