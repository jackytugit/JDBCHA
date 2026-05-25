package hademo;

import oracle.simplefan.*;
import java.util.Properties;

public class FANDemo {

    public static void main(String[] args) throws Exception {
        // FAN/ONS event daemon configuration
        Properties onsProps = new Properties();
        onsProps.setProperty("onsNodes", "host01:6200,host02:6200,host03:6200");


        FanManager fanManager = FanManager.getInstance();
        fanManager.configure(onsProps);

        // Subscribe to a service
        Properties subscriptionProps = new Properties();
        subscriptionProps.setProperty("serviceName", "ftselect");

        FanSubscription subscription = fanManager.subscribe(subscriptionProps);

        // Listen for service up/down, node up/down, and load advisory events
        FanEventListener listener = new FanUpEventListener() {

            @Override
            public void handleEvent(ServiceDownEvent event) {
                System.out.println("SERVICE DOWN");
                System.out.println("  time: " + event.getTimestamp());
                System.out.println("  service: " + event.getServiceName());
                System.out.println("  db: " + event.getDatabaseUniqueName());
                System.out.println("  reason: " + event.getReason());

                ServiceDownEvent.ServiceMemberEvent me = event.getServiceMemberEvent();
                if (me != null) {
                    System.out.println("  instance: " + me.getInstanceName());
                    System.out.println("  node: " + me.getNodeName());
                    //System.out.println("  status: " + me.getServiceMemberStatus());
                }

//                ServiceCompositeEvent ce = event.getServiceCompositeEvent();
//                if (ce != null) {
//                    System.out.println("  composite status: " + ce.getServiceCompositeStatus());
//                }
            }

            @Override
            public void handleEvent(NodeDownEvent event) {
                System.out.println("NODE DOWN");
                System.out.println("  time: " + event.getTimestamp());
                System.out.println("  node: " + event.getNodeName());
                System.out.println("  incarnation: " + event.getIncarnation());
            }

            @Override
            public void handleEvent(LoadAdvisoryEvent event) {
                System.out.println("LOAD ADVISORY");
                System.out.println("  time: " + event.getTimestamp());
                System.out.println("  service: " + event.getServiceName());
                System.out.println("  db: " + event.getDatabaseUniqueName());
                System.out.println("  instance: " + event.getInstanceName());
                System.out.println("  percent: " + event.getPercent());
                System.out.println("  quality: " + event.getServiceQuality());
                System.out.println("  load: " + event.getLoadStatus());
            }

            @Override
            public void handleEvent(ServiceUpEvent event) {
                System.out.println("SERVICE UP");
                System.out.println("  time: " + event.getTimestamp());
                System.out.println("  service: " + event.getServiceName());
                System.out.println("  db: " + event.getDatabaseUniqueName());
                System.out.println("  reason: " + event.getReason());
                System.out.println("  kind: " + event.getKind());
                System.out.println("  cardinality: " + event.getCardinality());

                ServiceUpEvent.ServiceMemberEvent me = event.getServiceMemberEvent();
                if (me != null) {
                    System.out.println("  instance: " + me.getInstanceName());
                    System.out.println("  node: " + me.getNodeName());
                    //System.out.println("  status: " + me.getServiceMemberStatus());
                }
            }

            @Override
            public void handleEvent(NodeUpEvent event) {
                System.out.println("NODE UP");
                System.out.println("  time: " + event.getTimestamp());
                System.out.println("  node: " + event.getNodeName());
                System.out.println("  incarnation: " + event.getIncarnation());
            }
        };

        subscription.addListener(listener);

        System.out.println("Listening for FAN events...");
        Thread.currentThread().join();
    }
}