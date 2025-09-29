package com.vetleDemo.store.model;

public class OAuthCallbackResponse {
    private String code;
    private String state;

    public OAuthCallbackResponse(String code, String state) {
        this.code = code;
        this.state = state;
    }

    public String getCode() { return code; }
    public String getState() { return state; }

    public void setCode(String code) { this.code = code; }
    public void setState(String state) { this.state = state; }

    @Override
    public String toString() {
        return "OAuthCallbackResponse{" +
                "code='" + code + '\'' +
                ", state='" + state + '\'' +
                '}';
    }
}
