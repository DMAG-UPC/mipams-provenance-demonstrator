package org.mipams.fake_media.demo.services;

import java.io.FileInputStream;
import java.io.IOException;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;

import org.mipams.fake_media.entities.ProvenanceSigner;
import org.mipams.jumbf.core.util.MipamsException;
import org.mipams.jumbf.crypto.entities.CryptoException;
import org.mipams.jumbf.crypto.services.KeyReaderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Service;

@Service
public class ProvenanceProducerService {
    @Autowired
    KeyReaderService keyReaderService;

    private ProvenanceSigner getProvenanceSigner(User user) throws MipamsException {
        Certificate cert = null;
        try (FileInputStream fis = new FileInputStream("");) {

            CertificateFactory cf = CertificateFactory.getInstance("X.509");

            while (fis.available() > 0) {
                cert = cf.generateCertificate(fis);
                System.out.println(cert.toString());
            }

            PublicKey pubKey = cert.getPublicKey();
            PrivateKey privKey = keyReaderService.getPrivateKey("");

            KeyPair kp = new KeyPair(pubKey, privKey);

            ProvenanceSigner signer = new ProvenanceSigner();
            signer.setSigningScheme("SHA1withRSA");
            signer.setSigningCredentials(kp);
            signer.setSigningCertificate(cert);

            return signer;
        } catch (IOException | CryptoException | CertificateException e) {
            throw new MipamsException(e);
        }
    }
}
