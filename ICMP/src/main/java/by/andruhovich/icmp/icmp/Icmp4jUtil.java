package by.andruhovich.icmp.icmp;

import by.andruhovich.icmp.icmp.constants.OsFamilyCode;
import by.andruhovich.icmp.icmp.platform.NativeBridge;
import by.andruhovich.icmp.icmp.util.PlatformUtil;

/**
 * icmp
 * http://www.icmp4j.org
 * Copyright 2009 and beyond, Sal Ingrilli at the icmp
 * <p/>
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License version 3
 * as published by the Free Software Foundation as long as:
 * 1. You credit the original author somewhere within your product or website
 * 2. The credit is easily reachable and not burried deep
 * 3. Your end-user can easily see it
 * 4. You register your name (optional) and company/group/org name (required)
 * at http://www.icmp4j.org
 * 5. You do all of the above within 4 weeks of integrating this software
 * 6. You contribute feedback, fixes, and requests for features
 * <p/>
 * If/when you derive a commercial gain from using this software
 * please donate at http://www.icmp4j.org
 * <p/>
 * If prefer or require, contact the author specified above to:
 * 1. Release you from the above requirements
 * 2. Acquire a commercial license
 * 3. Purchase a support contract
 * 4. Request a different license
 * 5. Anything else
 * <p/>
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE, similarly
 * to how this is described in the GNU Lesser General Public License.
 * <p/>
 * User: Sal Ingrilli
 * Date: Dec 23, 2014
 * Time: 10:51:44 PM
 */
public class Icmp4jUtil {

    // my attributes
    private static NativeBridge nativeBridge;

    // my attributes
    public static void setNativeBridge(final NativeBridge nativeBridge) {
        Icmp4jUtil.nativeBridge = nativeBridge;
    }

    public static NativeBridge getNativeBridge() {
        return nativeBridge;
    }

    /**
     * Uniformly initializes the Icmp subsystem
     * This is in an explicit method and NOT a static initializer so that the caller gets the full stack trace
     */
    public static void initialize() {

        // already initialized?
        if (nativeBridge != null) {
            return;
        }

        // support concurrency
        synchronized (Icmp4jUtil.class) {

            // already initialized?
            if (nativeBridge != null) {
                return;
            }

            // handle exceptions
            try {

                // initialize other components
                PlatformUtil.initialize();

                final int osFamilyCode = PlatformUtil.getOsFamilyCode();
                // osFamilyCode-specific processing
                // WARNING: do NOT include these classes otherwise when we build the platform-specific jars
                // like icmp-android.jar, it will not run because android complains that it includes
                // DLL references...
                //final int osFamilyCode = PlatformUtil.getOsFamilyCode ();
                final String nativeBridgeClassName =
                        osFamilyCode == OsFamilyCode.ANDROID ? "by.andruhovich.icmp.icmp.platform.android.AndroidNativeBridge" :
                                osFamilyCode == OsFamilyCode.LINUX ? "by.andruhovich.icmp.icmp.platform.unix.UnixNativeBridge" :
                                        osFamilyCode == OsFamilyCode.MAC ? "by.andruhovich.icmp.icmp.platform.unix.UnixNativeBridge" :
                                                osFamilyCode == OsFamilyCode.WINDOWS ? "by.andruhovich.icmp.icmp.platform.windows.WindowsNativeBridge" :
                                                        "by.andruhovich.icmp.icmp.platform.java.JavaNativeBridge";

                // objectify on the stack
                // warning: Wed 8/12/2015, bug report by daifeisg8@users.sf.net
                // this used to objectify directly onto nativeBridge and, as pointed out by it could cause threads
                // to enter initialize () and find a nativeBridge reference that has not yet been initialized!
                final Class<NativeBridge> nativeBridgeClass = (Class<NativeBridge>) Class.forName(nativeBridgeClassName);
                final NativeBridge nativeBridge = nativeBridgeClass.newInstance();

                // track
                Icmp4jUtil.nativeBridge = nativeBridge;

                nativeBridge.initialize();
            } catch (final Exception e) {

                // propagate
                throw new RuntimeException(e);
            }
        }
    }

    /**
     * Uniformly destroys the Icmp subsystem
     */
    public static void destroy() {

        // delegate
        if (nativeBridge != null) {
            nativeBridge.destroy();
            nativeBridge = null;
        }
    }
}