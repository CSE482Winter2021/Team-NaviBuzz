package com.navisens.demo.android_app_helloworld.utils;

public class Constants {
    // on longitude/latitude estimation without GPS, was noticing a consistent error on my phone.
    // adding a small bias fixes it, but should be tested on more phones
    public static final double BIAS = 0.000001;
    public static final double EARTH_RADIUS_KM = 6371.0088;
    public static final String NAVISENS_DEV_KEY = "hsW5F8tUr8nPLP1hgY2oj2Zy26iqZ7YCPK4mTEnTsNpj0l0yRwGfj33m3GUL0vCF";
    public static final int REQUEST_MDNA_PERMISSIONS=1;
}
