package by.andruhovich.icmp.icmp.platform.windows;

import by.andruhovich.icmp.exception.TechnicalException;
import by.andruhovich.icmp.icmp.IcmpPingRequest;
import by.andruhovich.icmp.icmp.IcmpPingResponse;
import by.andruhovich.icmp.icmp.IcmpPingUtil;
import by.andruhovich.icmp.icmp.platform.NativeBridge;
import by.andruhovich.icmp.icmp.platform.windows.jna.IcmpLibrary;
import by.andruhovich.icmp.icmp.platform.windows.jna.LibraryUtil;
import by.andruhovich.icmp.icmp.platform.windows.jna.Winsock2Library;
import com.sun.jna.Memory;
import com.sun.jna.Pointer;
import com.sun.jna.platform.win32.Kernel32;

import java.net.InetAddress;

public class WindowsNativeBridge extends NativeBridge {
    @Override
    public void initialize() {

        // delegate
        LibraryUtil.initialize();

        // initialize winsock2
        final Winsock2Library winsock2Library = LibraryUtil.getWinsock2Library();
        final Winsock2Library.WSAData wsadata = new Winsock2Library.WSAData();
        if (winsock2Library.WSAStartup((short) 2, wsadata) != 0) {
            throw new RuntimeException("WSAStartup failed");
        }
    }

    @Override
    public void destroy() {

        // destroy winsock2
        final Winsock2Library winsock2Library = LibraryUtil.getWinsock2Library();
        if (winsock2Library != null) {
            winsock2Library.WSACleanup();
        }
    }

    @Override
    public IcmpPingResponse executePingRequest(final IcmpPingRequest request) throws TechnicalException {

        // helpers
        final IcmpLibrary icmpLibrary = LibraryUtil.getIcmpLibrary();
        final String host = request.getHost();
        final int ttl = request.getTtl();
        final int size = request.getPacketSize();
        final int timeout = Long.valueOf(request.getTimeout()).intValue();

        // handle exceptions
        Pointer hIcmp = null;
        try {

            // request
            final InetAddress address = InetAddress.getByName(host);
            final byte[] ipAddressAsByteArray = address.getAddress();

            final IcmpLibrary.IpAddrByVal ipaddr = new IcmpLibrary.IpAddrByVal();
            ipaddr.bytes = ipAddressAsByteArray;

            final int replyDataSize = size + (new IcmpLibrary.IcmpEchoReply().size());
            Pointer sendData = new Memory(size);
            Pointer replyData = new Memory(replyDataSize);
            IcmpLibrary.IpOptionInformationByRef ipOption = new IcmpLibrary.IpOptionInformationByRef();
            ipOption.ttl = (byte) ttl;
            ipOption.tos = (byte) 0;
            ipOption.flags = (byte) 0;
            ipOption.optionsSize = (byte) 0;
            ipOption.optionsData = null;

            // delegate
            hIcmp = icmpLibrary.IcmpCreateFile();
            final long icmpSendEchoStartTime = System.currentTimeMillis();
            final int returnCode = icmpLibrary.IcmpSendEcho(
                    hIcmp,
                    ipaddr,
                    sendData,
                    (short) size,
                    ipOption,
                    replyData,
                    replyDataSize,
                    timeout);
            final long icmpSendEchoDuration = System.currentTimeMillis() - icmpSendEchoStartTime;

            // check for failure
            final IcmpLibrary.IcmpEchoReply echoReply = new IcmpLibrary.IcmpEchoReply(replyData);
            if (returnCode == 0 && echoReply.status == 0) {

                // call failed
                final int lastError = Kernel32.INSTANCE.GetLastError();
                final String errorMessage = "Last error: " + lastError;

                // objectify
                final IcmpPingResponse response = new IcmpPingResponse();
                response.setErrorMessage(errorMessage);
                response.setSuccessFlag(false);

                // done
                return response;

            }

            // check for timeout timeout
            if (echoReply.status == 11010) {
                return IcmpPingUtil.createTimeoutIcmpPingResponse(icmpSendEchoDuration);
            }

            // response
            final boolean successFlag = echoReply.status == 0;
            final String responseAddress = String.format("%d.%d.%d.%d",
                    echoReply.address.bytes[0] & 0xff,
                    echoReply.address.bytes[1] & 0xff,
                    echoReply.address.bytes[2] & 0xff,
                    echoReply.address.bytes[3] & 0xff
            );
            final String message = getStatusText(echoReply.status);

            // objectify
            final IcmpPingResponse response = new IcmpPingResponse();
            response.setHost(responseAddress);
            response.setErrorMessage(message);
            response.setRtt(echoReply.roundTripTime);
            response.setSize(echoReply.dataSize);
            response.setSuccessFlag(successFlag);
            response.setTtl(echoReply.options.ttl & 0xff);

            // done
            return response;
        } catch (final Exception e) {

            // propagate
            throw new TechnicalException(e);
        } finally {

            // cleanup
            if (hIcmp != null) {
                icmpLibrary.IcmpCloseHandle(hIcmp);
            }
        }
    }

    private static String getStatusText(final int status) {
        switch (status) {
            case 0:
                return "SUCCESS";
            case 11000:
                return "IP_SUCCESS";
            case 11001:
                return "IP_BUF_TOO_SMALL";
            case 11002:
                return "IP_DEST_NET_UNREACHABLE";
            case 11003:
                return "IP_DEST_HOST_UNREACHABLE";
            case 11004:
                return "IP_DEST_PROT_UNREACHABLE";
            case 11005:
                return "IP_DEST_PORT_UNREACHABLE";
            case 11006:
                return "IP_NO_RESOURCES";
            case 11007:
                return "IP_BAD_OPTION";
            case 11008:
                return "IP_HW_ERROR";
            case 11009:
                return "IP_PACKET_TOO_BIG";
            case 11010:
                return "IP_REQ_TIMED_OUT";
            case 11011:
                return "IP_BAD_REQ";
            case 11012:
                return "IP_BAD_ROUTE";
            case 11013:
                return "IP_TTL_EXPIRED_TRANSIT";
            case 11014:
                return "IP_TTL_EXPIRED_REASSEM";
            case 11015:
                return "IP_PARAM_PROBLEM";
            case 11016:
                return "IP_SOURCE_QUENCH";
            case 11017:
                return "IP_OPTION_TOO_BIG";
            case 11018:
                return "IP_BAD_DESTINATION";
            case 11050:
                return "IP_GENERAL_FAILURE";
        }
        return "UNKNOWN_STATUS";
    }
}