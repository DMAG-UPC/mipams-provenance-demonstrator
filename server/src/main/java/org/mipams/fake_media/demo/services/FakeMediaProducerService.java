package org.mipams.fake_media.demo.services;

import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.X509Certificate;

import org.mipams.fake_media.entities.ProvenanceSigner;
import org.mipams.jumbf.core.util.CoreUtils;
import org.mipams.jumbf.core.util.MipamsException;
import org.mipams.jumbf.crypto.entities.CryptoException;
import org.mipams.jumbf.crypto.services.CredentialsReaderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

@Service
public class FakeMediaProducerService {

    final static String CERTIFICATE_FILENAME_FORMAT = "%s.crt";
    final static String KEY_FILENAME_FORMAT = "%s.priv.key";

    @Autowired
    CredentialsReaderService credentialsReaderService;

    @Autowired
    UserDetails userDetails;

    @Value("${org.mipams.fake_media.demo.credentials_path}")
    String CREDENTIALS_PATH;

    @Value("${org.mipams.fake_media.demo.claim_generator}")
    String CLAIM_GENERATOR_DESCRIPTION;

    private ProvenanceSigner getProvenanceSigner(User user) throws MipamsException {

        final String userCredentialsPath = CoreUtils.getFullPath(CREDENTIALS_PATH, user.getUsername());

        final String userCertificateUrl = CoreUtils.getFullPath(userCredentialsPath,
                String.format(CERTIFICATE_FILENAME_FORMAT, user.getUsername()));

        final String userKeyUrl = CoreUtils.getFullPath(userCredentialsPath,
                String.format(KEY_FILENAME_FORMAT, user.getUsername()));

        try {
            X509Certificate cert = credentialsReaderService.getCertificate(userCertificateUrl);

            PublicKey pubKey = cert.getPublicKey();
            PrivateKey privKey = credentialsReaderService.getPrivateKey(userKeyUrl);

            KeyPair kp = new KeyPair(pubKey, privKey);

            ProvenanceSigner signer = new ProvenanceSigner();
            signer.setSigningScheme("SHA1withRSA");
            signer.setSigningCredentials(kp);
            signer.setSigningCertificate(cert);

            return signer;
        } catch (CryptoException e) {
            throw new MipamsException(e);
        }
    }
}
