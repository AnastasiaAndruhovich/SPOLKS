package by.andruhovich.icmp.start;

import by.andruhovich.icmp.icmp.IcmpPingRequest;
import by.andruhovich.icmp.icmp.IcmpPingResponse;
import by.andruhovich.icmp.icmp.IcmpPingUtil;
import by.andruhovich.icmp.traceroute.TraceroutePane;

import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Start {
    public static void main(String args[]) {
        ArrayList<String> devices = new ArrayList<>();
        devices.add("google.com");
        devices.add("vk.com");
        devices.add("yandex.com");

        ExecutorService es = Executors.newFixedThreadPool(devices.size());
        for (final String device : devices) {
            es.submit(() -> {
                try {
                    final IcmpPingRequest request = IcmpPingUtil.createIcmpPingRequest();
                    request.setHost(device);
                    for (int count = 1; count <= 4; count ++) {

                        // delegate
                        final IcmpPingResponse response = IcmpPingUtil.executePingRequest (request);

                        // log
                        final String formattedResponse = IcmpPingUtil.formatResponse (response);
                        System.out.println (device + " " + formattedResponse);

                        // rest
                        Thread.sleep (1000);
                    }
                } catch (Exception e) {
                    System.out.println(e.getMessage());
                }
            });
        }
        es.shutdown();
        while (!es.isTerminated()) {
        }

        System.loadLibrary("jpcap");
        System.loadLibrary("wpcap");
        final String[] copyOfArgs = args;

        java.awt.EventQueue.invokeLater(() -> new TraceroutePane(copyOfArgs).setVisible(true));
    }
}
