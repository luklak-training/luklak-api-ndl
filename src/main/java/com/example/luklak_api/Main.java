package com.example.luklak_api;

import com.example.luklak_api.verticle.ApiVerticle;
import com.hazelcast.config.Config;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.eventbus.EventBusOptions;
import io.vertx.core.spi.cluster.ClusterManager;
import io.vertx.spi.cluster.hazelcast.HazelcastClusterManager;
import lombok.extern.slf4j.Slf4j;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

@Slf4j
public class Main {

    public static void main(String[] args) {
        Config hazelcastConfig = new Config();
        ClusterManager mgr
                = new HazelcastClusterManager(hazelcastConfig);
        VertxOptions options = new VertxOptions()
                .setClusterManager(mgr);

        String containerAddress = getAddress();

        log.info("Container Address "+containerAddress);

        EventBusOptions ebOptions = new EventBusOptions()
                .setHost(containerAddress);

        options.setEventBusOptions(ebOptions);

        Vertx.clusteredVertx(options, handler-> {
            if (handler.succeeded()) {
                handler.result()
                        .deployVerticle(ApiVerticle.class,
                                new DeploymentOptions().setInstances(3),
                                deployHandler->{
                                    if(handler.succeeded()){
                                        log.info
                                                ("Host Address "+ebOptions.getHost());
                                        log.info
                                                ("Verticle Deployed");
                                    }else{
                                        log.error
                                                ("Verticle Deployment Failed");
                                    }
                                });
            }
        });
    }

    private static String getAddress()  {
        try {
            List<NetworkInterface> networkInterfaces =
                    new ArrayList<>();
            NetworkInterface.getNetworkInterfaces()
                    .asIterator().forEachRemaining(networkInterfaces::add);
            return networkInterfaces.stream()
                    .flatMap(iface->iface.inetAddresses()
                            .filter(entry->entry.getAddress().length == 4)
                            .filter(entry->!entry.isLoopbackAddress())
                            .filter(entry->entry.getAddress()[0] !=
                                    Integer.valueOf(10).byteValue())
                            .map(InetAddress::getHostAddress))
                    .findFirst().orElse(null);
        } catch (SocketException e) {
            return null;
        }
    }
}
