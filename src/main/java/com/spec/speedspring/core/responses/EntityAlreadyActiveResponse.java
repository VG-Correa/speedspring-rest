package com.spec.speedspring.core.responses;

import java.util.ArrayList;

import lombok.Data;

@Data
public class EntityAlreadyActiveResponse extends GenericResponse{

    public EntityAlreadyActiveResponse(String message) {
        setStatus(400);
        setError(true);
        setMessage(message);
        setData(new ArrayList<>());
        setMetadata(null);
    }

}