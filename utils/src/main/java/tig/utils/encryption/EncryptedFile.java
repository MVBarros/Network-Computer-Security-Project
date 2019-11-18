package tig.utils.encryption;

public class EncryptedFile {
    private byte[] content;
    private byte[] iv;

    public byte[] getContent() {
        return content;
    }

    public byte[] getIv() {
        return iv;
    }

    public EncryptedFile(byte[] content, byte[] iv) {
        this.content = content;
        this.iv = iv;
    }

}
