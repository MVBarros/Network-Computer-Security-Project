package tig.utils.keys;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.spec.PKCS8EncodedKeySpec;

public class KeyFileLoader {

    public static PublicKey loadPublicKey(File certChainFile) throws Exception {
        CertificateFactory factory = CertificateFactory.getInstance("X.509");
        X509Certificate certificate = (X509Certificate) factory.generateCertificate(new FileInputStream(certChainFile));
        return certificate.getPublicKey();
    }

    public static PrivateKey loadPrivateKey(File privateKeyFile) throws Exception {
        byte[] keyBytes = Files.readAllBytes(Paths.get(privateKeyFile.getPath()));
        PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(keyBytes);
        KeyFactory kf = KeyFactory.getInstance("RSA");
        return kf.generatePrivate(spec);
    }
}