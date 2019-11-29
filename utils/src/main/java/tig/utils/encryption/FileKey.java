package tig.utils.encryption;

public class FileKey {


    private byte[] key;
    private byte[] iv;
    private String id;

    public byte[] getKey() {
        return key;
    }

    public byte[] getIv() {
        return iv;
    }

    public String getId() { return id; }

    public FileKey(byte[] key, byte[] iv, String id) {
        this.key = key;
        this.iv = iv;
        this.id = id;
    }


}
