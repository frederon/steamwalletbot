package com.frederon;

public class Wallet {
    public String id;
    public String code;

    public Wallet(String id, String code) {
        this.id = id;
        this.code = code;
    }

    @Override
    public String toString() {
        return "Wallet{" +
                "id='" + id + '\'' +
                ", code='" + code + '\'' +
                '}';
    }

    public String getCode() {
        return code;
    }

    public String getId() {
        return id;
    }
}
