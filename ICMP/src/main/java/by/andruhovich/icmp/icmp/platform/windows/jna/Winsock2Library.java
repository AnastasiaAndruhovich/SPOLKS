package by.andruhovich.icmp.icmp.platform.windows.jna;

import com.sun.jna.Library;
import com.sun.jna.Pointer;
import com.sun.jna.Structure;

import java.util.Arrays;
import java.util.List;

public interface Winsock2Library extends Library {

  public static class WSAData extends Structure {
    public short version;
    public short highVersion;
    public byte[] description = new byte[256+1];
    public byte[] systemStatus = new byte[256+1];
    public short maxSockets;
    public short maxUdpDg;
    public Pointer vendorInfo;
    
    @Override
    protected List<String> getFieldOrder() {
      return Arrays.asList ("version", "highVersion", "description", "systemStatus", "maxSockets", "maxUdpDg", "vendorInfo");
    }
  }

  public static class Hostent extends Structure {
    public Pointer name;
    public Pointer aliases;
    public short addrType;
    public short length;
    public Pointer addressList;
    
    @Override
    protected List<String> getFieldOrder() {
      return Arrays.asList ("name", "aliases", "addrType", "length", "addressList");
    }
  }

  int WSAStartup(short versionRequested, WSAData wsadata);

  int WSACleanup();

  Hostent gethostbyname(String name);
}