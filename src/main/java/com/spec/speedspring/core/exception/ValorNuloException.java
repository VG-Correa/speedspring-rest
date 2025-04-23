package com.spec.speedspring.core.exception;

import io.micrometer.common.lang.Nullable;

public class ValorNuloException extends RuntimeException {

    public ValorNuloException(@Nullable String msg) {
        super(msg);
    }

}
