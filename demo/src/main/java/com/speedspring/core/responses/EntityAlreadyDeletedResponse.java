package com.speedspring.core.responses;

import java.util.ArrayList;

public class EntityAlreadyDeletedResponse extends GenericResponse {

    public EntityAlreadyDeletedResponse(String message) {
        setStatus(400);
        setError(true);
        setMessage(message);
        setData(new ArrayList<>());
        setMetadata(null);
    }

}