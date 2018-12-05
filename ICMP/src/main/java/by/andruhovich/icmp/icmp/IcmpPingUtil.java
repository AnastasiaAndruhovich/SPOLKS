package by.andruhovich.icmp.icmp;

import by.andruhovich.icmp.exception.TechnicalException;
import by.andruhovich.icmp.icmp.platform.NativeBridge;
import by.andruhovich.icmp.icmp.platform.windows.WindowsNativeBridge;

public class IcmpPingUtil {
  
  // my attributes
  private static NativeBridge nativeBridge;
  
  // my attributes
  public static void setNativeBridge (final NativeBridge nativeBridge) { IcmpPingUtil.nativeBridge = nativeBridge; }
  public static NativeBridge getNativeBridge () { return nativeBridge; }

  public static void initialize () {
    
    // already initialized?
    if (nativeBridge != null) {
      return;
    }

    // support concurrency
    synchronized (IcmpPingUtil.class) {
      
      // already initialized?
      if (nativeBridge != null) {
        return;
      }

      // initialize
      // platform-specific processing
      nativeBridge = new WindowsNativeBridge();
      nativeBridge.initialize ();
    }
  }

  public static void destroy () {
    
    // delegate
    if (nativeBridge != null) {
      nativeBridge.destroy ();
      nativeBridge = null;
    }
  }

  public static IcmpPingRequest createIcmpPingRequest () {

    final IcmpPingRequest request = new IcmpPingRequest ();
    request.setHost ("localhost");
    request.setPacketSize (32);
    request.setTimeout (5000);
    request.setTtl (255);

    // done
    return request;
  }

  public static IcmpPingResponse createTimeoutIcmpPingResponse (final long duration) {
    
    // objectify
    final IcmpPingResponse response = new IcmpPingResponse ();
    response.setErrorMessage ("Timeout reached after " + duration + " msecs");
    response.setSuccessFlag (false);
    response.setTimeoutFlag (true);

    // done
    return response;
  }

  public static IcmpPingResponse executePingRequest (final IcmpPingRequest request) throws TechnicalException {

    // jit-initialize
    initialize ();
    
    // assert preconditions
    {
      final String host = request.getHost ();
      if (host == null) {
        throw new RuntimeException ("host must be specified");
      }
    }
    
    // assert preconditions
    {
      final int packetSize = request.getPacketSize ();
      if (packetSize == 0) {
        throw new RuntimeException ("packetSize must be > 0: " + packetSize);
      }
    }
    
    // delegate
    final IcmpPingResponse response = nativeBridge.executePingRequest (request);
    
    // postconditions: rtt should not be a crazy value
    final int rtt = response.getRtt ();
    if (rtt == Integer.MAX_VALUE) {
      throw new RuntimeException ("rtt should not be MAX_VALUE: " + rtt);
    }

    // postconditions: rtt should not be > timeout
    final long timeout = request.getTimeout ();
    if (timeout > 0 && rtt > timeout) {
      throw new RuntimeException ("rtt should not be > timeout: " + rtt + " / " + timeout);
    }
    
    // done
    return response;
  }

  public static IcmpPingResponse executePingRequest (
    final String host,
    final int packetSize,
    final long timeout) throws TechnicalException {

    // delegate
    final IcmpPingRequest request = IcmpPingUtil.createIcmpPingRequest ();
    request.setHost (host);
    request.setPacketSize (packetSize);
    request.setTimeout (timeout);
    return executePingRequest (request);
  }

  public static String formatResponse (final IcmpPingResponse response) {

    // request
    final boolean successFlag = response.getSuccessFlag ();
    final String address = response.getHost ();
    final String message = response.getErrorMessage ();
    final int size = response.getSize ();
    final int rtt = response.getRtt ();
    final int ttl = response.getTtl ();

    return successFlag ?
      "Reply from " + address + ": bytes=" + size + " time=" + rtt + "ms TTL=" + ttl :
      "Error: " + message;
  }
}