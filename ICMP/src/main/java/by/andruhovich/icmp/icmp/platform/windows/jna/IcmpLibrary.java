package by.andruhovich.icmp.icmp.platform.windows.jna;

import com.sun.jna.Library;
import com.sun.jna.Pointer;
import com.sun.jna.Structure;
import com.sun.jna.Structure.ByReference;
import com.sun.jna.Structure.ByValue;

import java.util.Arrays;
import java.util.List;

public interface IcmpLibrary extends Library {

  public static class IpAddr extends Structure {

    public byte[] bytes = new byte[4];
    
    @Override
    protected List<String> getFieldOrder() {
      return Arrays.asList (new String[] {"bytes"});
    }
  }

  public static class IpAddrByVal extends IpAddr implements ByValue {

    @Override
    protected List<String> getFieldOrder() {
      return Arrays.asList (new String[] {"bytes"});
    }
  }

  public static class IpOptionInformation extends Structure {
    public byte ttl;
    public byte tos;
    public byte flags;
    public byte optionsSize;
    public Pointer optionsData;
    
    @Override
    protected List<String> getFieldOrder() {
      return Arrays.asList (new String[] {"ttl", "tos", "flags", "optionsSize", "optionsData"});
    }
  }

  public static class IpOptionInformationByVal 
      extends IpOptionInformation implements ByValue {
  }

  public static class IpOptionInformationByRef
      extends IpOptionInformation implements ByReference {
  }

  public static class IcmpEchoReply extends Structure {
    public IpAddrByVal address;
    public int status;
    public int roundTripTime;
    public short dataSize;
    public short reserved;
    public Pointer data;
    public IpOptionInformationByVal options;

    public IcmpEchoReply(){
    }

    public IcmpEchoReply(Pointer p){
      useMemory(p);
      read();
    }
    
    @Override
    protected List<String> getFieldOrder() {
      return Arrays.asList (new String[] {"address", "status", "roundTripTime", "dataSize", "reserved", "data", "options"});
    }
  }

  public Pointer IcmpCreateFile();

  public boolean IcmpCloseHandle(Pointer hIcmp);

  public int IcmpSendEcho(
          Pointer hIcmp,
          IpAddrByVal destinationAddress,
          Pointer requestData,
          short requestSize,
          IpOptionInformationByRef requestOptions,
          Pointer replyBuffer,
          int replySize,
          int timeout
  );
}