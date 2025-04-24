package com.spec.speedspring.core.responses;

import java.util.ArrayList;
import java.util.List;


public class GenericResponse {

    private Integer status;
    private boolean error;
    private String message;
    private Object metadata;
    private Object data;

    @SuppressWarnings({ "rawtypes", "unchecked" })
    public GenericResponse setData(Object obj) {
        try {
            ((List) obj).size();
            data = obj;
        } catch (Exception err) {
            data = new ArrayList<Object>();
            ((ArrayList) data).add(obj);
        }
        return this;
    }

    public GenericResponse setMetadata(Object metadata) {
        this.metadata = metadata;
        return this;
    }

    public GenericResponse setStatus(Integer status) {
        this.status = status;
        return this;
    }

    public GenericResponse setError(boolean error) {
        this.error = error;
        return this;
    }

    public GenericResponse setMessage(String message) {
        this.message = message;
        return this;
    }

    public GenericResponse(){}

    public GenericResponse(GenericResponse gr) {
        setStatus(gr.status);
        setError(gr.error);
        setData(gr.data);
        setMessage(gr.message);
        setMetadata(gr.metadata);
    }

    public GenericResponse build() {
        GenericResponse genericResponse = new GenericResponse(this);

        this.data = new ArrayList<>();
        this.error = false;
        this.message = null;
        this.metadata = null;
        this.status = null;

        return genericResponse;
    }

    public Integer getStatus() {
        return status;
    }

    public boolean isError() {
        return error;
    }

    public String getMessage() {
        return message;
    }

    public Object getMetadata() {
        return metadata;
    }

    public Object getData() {
        return data;
    }

}