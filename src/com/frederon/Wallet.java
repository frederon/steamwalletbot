package com.frederon;

class Wallet {
    private String id;
    private String code;

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
