package by.andruhovich.icmp.icmp.platform.windows.jna;

import com.sun.jna.Native;

public class LibraryUtil {
  private static IcmpLibrary icmpLibrary;
  private static Winsock2Library winsock2Library;

  public static IcmpLibrary getIcmpLibrary () { return icmpLibrary; }
  public static Winsock2Library getWinsock2Library () { return winsock2Library; }

  public static void initialize () {
    if (icmpLibrary == null) {
      icmpLibrary = (IcmpLibrary) Native.loadLibrary ("icmp", IcmpLibrary.class);
    }

    if (winsock2Library == null) {
      winsock2Library = (Winsock2Library) Native.loadLibrary ("ws2_32", Winsock2Library.class);
    }
  }
}