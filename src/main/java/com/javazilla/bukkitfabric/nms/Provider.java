package com.javazilla.bukkitfabric.nms;

import java.io.File;

public interface Provider {

    public boolean visitFieldInsn(int opcode, String owner, String name, String desc);

    public boolean visitMethodInsn(int opcode, String owner, String name, String desc, boolean itf);

    public boolean remap(File jarFile);

    public boolean runSpecialSource(File mappingsFile, File inJar, File outJar);

}
