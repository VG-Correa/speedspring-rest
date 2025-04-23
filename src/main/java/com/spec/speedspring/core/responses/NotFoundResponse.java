package com.spec.speedspring.core.responses;

import java.util.ArrayList;

import lombok.Data;

@Data
public class NotFoundResponse extends GenericResponse{

    public NotFoundResponse(Exception e) {
        setMessage(e.getMessage());
        setData(new ArrayList<>());
        setStatus(404);
        setError(true);
    }

}