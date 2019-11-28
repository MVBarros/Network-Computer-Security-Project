package tig.utils.encryption;

public class FileKey {


    private byte[] key;
    private byte[] iv;

    public byte[] getKey() {
        return key;
    }

    public byte[] getIv() {
        return iv;
    }

    public FileKey(byte[] key, byte[] iv) {
        this.key = key;
        this.iv = iv;
    }


}
