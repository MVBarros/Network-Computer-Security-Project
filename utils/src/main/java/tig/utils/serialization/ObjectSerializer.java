package tig.utils.serialization;

import java.io.*;

public class ObjectSerializer {

    public static byte[] Serialize(Object obj) {
        try (ByteArrayOutputStream bos = new ByteArrayOutputStream();
             ObjectOutput out = new ObjectOutputStream(bos)) {
            out.writeObject(obj);
            return bos.toByteArray();
        } catch (Exception e) {
            //Should never happen
            throw new RuntimeException();
        }
    }

    public static Object Deserialize(byte[] bytes) {
        try (ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
             ObjectInput in = new ObjectInputStream(bis)) {
            return in.readObject();
        } catch (Exception e) {
            //Should never happen
            throw new RuntimeException();
        }
    }
}

