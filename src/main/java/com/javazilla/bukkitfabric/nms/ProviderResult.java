package com.javazilla.bukkitfabric.nms;

public class ProviderResult {

    public int opcode;
    public String owner;
    public String name;
    public String desc;

    public boolean changed;

    public ProviderResult(int a, String b, String c, String d) {
        this.opcode = a;
        this.owner = b;
        this.name = c;
        this.desc = d;
        this.changed = false;
    }

}