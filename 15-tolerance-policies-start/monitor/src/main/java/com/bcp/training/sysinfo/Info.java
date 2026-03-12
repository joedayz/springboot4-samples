package com.bcp.training.sysinfo;

public class Info {
    public final String NAME = System.getProperty("os.name");
    public final String ARCH = System.getProperty("os.arch");
    public final String VERSION = System.getProperty("os.version");
}
