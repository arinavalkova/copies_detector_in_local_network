import java.io.IOException;
import java.net.*;
import java.util.*;

public class Main extends Consts {
    private static final Map<String, Long> aliveIpAddresses = Collections.synchronizedMap(new HashMap<>());

    static MulticastSocket firstSocket;
    static MulticastSocket secondSocket;

    static InetAddress group;

    static DatagramPacket packet;
    static DatagramPacket receivedMsg;

    public static void main(String[] args) {
        try {
            String localNetworkIpAddress = getIpAddress(args);

            group = InetAddress.getByName(localNetworkIpAddress);
            packet = new DatagramPacket(MINE_MESSAGE.getBytes(), MINE_MESSAGE.length(), group, LOCAL_NETWORK_PORT);

            byte[] buffer = new byte[BUFFER_SIZE];
            receivedMsg = new DatagramPacket(buffer, buffer.length);

            joinSockets();

            initSendThread();
            initReceiveThread();
            initCheckIpMapThread();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static String getIpAddress(String[] args) {
        if (args.length < 2) {
            return IP4_ADDRESS;
        }

        return args[1];
    }

    private static void joinSockets() throws IOException {
        initSockets();
        joinSocketsToGroup();
    }

    private static void initSockets() throws IOException {
        firstSocket = new MulticastSocket();
        secondSocket = new MulticastSocket(LOCAL_NETWORK_PORT);

    }

    private static void joinSocketsToGroup() throws IOException {
        firstSocket.joinGroup(group);
        secondSocket.joinGroup(group);
    }

    private static void initSendThread() {
        Runnable task = () -> {
            while (true) {
                try {
                    firstSocket.send(packet);
                    Thread.sleep(ONE_SEC);
                } catch (InterruptedException | IOException e) {
                    e.printStackTrace();
                }
            }
        };
        Thread thread1 = new Thread(task);
        thread1.start();
    }

    private static void initReceiveThread() {
        Runnable task2 = () -> {
            while (true) {
                try {
                    secondSocket.receive(receivedMsg);
                    if (!isReceivedMsgMine(receivedMsg))
                        continue;

                    String addressFromReceivedMessage = String.valueOf(receivedMsg.getSocketAddress());
                    addingToMap(addressFromReceivedMessage, System.currentTimeMillis());

                    printAliveIpAddresses();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        };
        Thread thread2 = new Thread(task2);
        thread2.start();
    }

    private static boolean isReceivedMsgMine(DatagramPacket receivedMsg) {
        String gotMessage = new String(receivedMsg.getData()).trim();
        return gotMessage.equals(MINE_MESSAGE);
    }

    private static void addingToMap(String addressFromReceivedMessage, long currentTimeMillis) {
        synchronized (aliveIpAddresses) {
            aliveIpAddresses.put(addressFromReceivedMessage, currentTimeMillis);
        }
    }

    private static void printAliveIpAddresses() {
        System.out.println("///myAddresses///");

        synchronized (aliveIpAddresses) {
            for (String key : aliveIpAddresses.keySet()) {
                System.out.println(key);
            }
        }

        System.out.println("/////////////////\n");
    }

    private static void initCheckIpMapThread() {
        Runnable task3 = () -> {
            while (true) {
                checkingAliveAddressesMap(System.currentTimeMillis());
            }
        };
        Thread thread3 = new Thread(task3);
        thread3.start();
    }

    private static void checkingAliveAddressesMap(long currentTimeMillis) {
        synchronized (aliveIpAddresses) {
            aliveIpAddresses.entrySet().removeIf(entry -> currentTimeMillis - entry.getValue() > TWO_SEC);
        }
    }
}