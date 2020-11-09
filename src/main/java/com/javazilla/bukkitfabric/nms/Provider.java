package com.javazilla.bukkitfabric.nms;

import java.io.File;

public class Provider {

    public boolean visitFieldInsn(int opcode, String owner, String name, String desc) {
        return false;
    }

    public boolean visitMethodInsn(int opcode, String owner, String name, String desc, boolean itf) {
        return false;
    }

    public boolean remap(File jarFile) {
        return false;
    }

    public boolean runSpecialSource(File mappingsFile, File inJar, File outJar) {
        return false;
    }

}
