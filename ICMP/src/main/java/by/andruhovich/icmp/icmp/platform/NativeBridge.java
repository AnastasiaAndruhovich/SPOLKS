package by.andruhovich.icmp.icmp.platform;

import by.andruhovich.icmp.exception.TechnicalException;
import by.andruhovich.icmp.icmp.IcmpPingRequest;
import by.andruhovich.icmp.icmp.IcmpPingResponse;

public abstract class NativeBridge {

  public void initialize () {
    
  }

  public void destroy () {
    
  }

  public abstract IcmpPingResponse executePingRequest (final IcmpPingRequest request) throws TechnicalException;
}