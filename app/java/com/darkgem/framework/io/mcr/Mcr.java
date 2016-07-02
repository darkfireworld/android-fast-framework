package com.darkgem.framework.io.mcr;

/**
 * 手机通讯录
 */
public class Mcr {
    String phone;
    String name;

    public Mcr(String phone, String name) {
        this.phone = phone;
        this.name = name;
    }

    public String getPhone() {
        return phone;
    }

    public String getName() {
        return name;
    }
}
