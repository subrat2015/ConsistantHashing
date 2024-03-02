import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;

/*
To implement consistent hashing in Java, we’ll use the TreeMap class to represent the hash ring.
The TreeMap class is a sorted map that maps the hash values to server names. We'll use the MD5
hash function to generate the hash values, which is a widely-used hash function that
produces a 128-bit hash value. To add a server to the ring, we'll generate multiple virtual nodes
 for that server and add them to the ring. We’ll also use a hash function to generate the
 name of the virtual node by appending a unique identifier to the server name.
 */
public class ConsistentHashing {
    private final TreeMap<Long, String> ring;
    private final int numberOfReplicas;
    private final MessageDigest md;

    public ConsistentHashing(int numberOfReplicas) throws NoSuchAlgorithmException {
        this.ring = new TreeMap<>();
        this.numberOfReplicas = numberOfReplicas;
        this.md = MessageDigest.getInstance("MD5");
    }

    public void addServer(String server) {
        for (int i = 0; i < numberOfReplicas; i++) {
            long hash = generateHash(server + i);
            ring.put(hash, server);
        }
    }

    public void removeServer(String server) {
        for (int i = 0; i < numberOfReplicas; i++) {
            long hash = generateHash(server + i);
            ring.remove(hash);
        }
    }

    public String getServer(String key) {
        if (ring.isEmpty()) {
            return null;
        }
        long hash = generateHash(key);
        if (!ring.containsKey(hash)) {
            SortedMap<Long, String> tailMap = ring.tailMap(hash);
            hash = tailMap.isEmpty() ? ring.firstKey() : tailMap.firstKey();
        }
        return ring.get(hash);
    }

    /*
    We'll use the MD5 hash function to generate the hash values, which is a
    widely-used hash function that produces a 128-bit hash value
     */
    private long generateHash(String key) {
        md.reset();
        md.update(key.getBytes());
        byte[] digest = md.digest();
        long hash = ((long) (digest[3] & 0xFF) << 24) |
                ((long) (digest[2] & 0xFF) << 16) |
                ((long) (digest[1] & 0xFF) << 8) |
                ((long) (digest[0] & 0xFF));
        return hash;
    }

    public static void main(String[] args) throws NoSuchAlgorithmException {
        ConsistentHashing ch = new ConsistentHashing(3);
        ch.addServer("server1");
        ch.addServer("server2");
        ch.addServer("server3");


        System.out.println("key1: is present on server: " + ch.getServer("key1"));
        System.out.println("key67890: is present on server: " + ch.getServer("key67890"));

        ch.removeServer("server1");
        System.out.println("After removing server1");

        System.out.println("key1: is present on server: " + ch.getServer("key1"));
        System.out.println("key67890: is present on server: " + ch.getServer("key67890"));
    }
}