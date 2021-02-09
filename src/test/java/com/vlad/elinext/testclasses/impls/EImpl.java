package com.vlad.elinext.testclasses.impls;

import com.vlad.elinext.annotation.Inject;
import com.vlad.elinext.testclasses.interfaces.A;
import com.vlad.elinext.testclasses.interfaces.E;

public class EImpl implements E {
    @Inject
    public EImpl() {

    }

    @Inject
    public EImpl(A a) {

    }
}
