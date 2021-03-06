package com.ex;

import eu.europa.esig.dss.*;
import eu.europa.esig.dss.client.tsp.OnlineTSPSource;
import eu.europa.esig.dss.pades.PAdESSignatureParameters;
import eu.europa.esig.dss.pades.signature.PAdESService;
import eu.europa.esig.dss.token.*;
import eu.europa.esig.dss.validation.CommonCertificateVerifier;
import eu.europa.esig.dss.x509.CertificateToken;
import org.apache.commons.io.IOUtils;
import org.springframework.stereotype.Service;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.KeyStore;
import java.util.Date;
import java.util.List;

/**
 * Created by Bartek on 29.11.2016.
 */
@Service
public class KeyStoreService {

    private AbstractSignatureTokenConnection signingToken;
    private DSSPrivateKeyEntry privateKey;

    public KeyStoreService() {

        /*signingToken = new MSCAPISignatureToken();
        List<DSSPrivateKeyEntry> list = signingToken.getKeys();

        privateKey = list.get(0);*/

           /* String pkcs12TokenFile = "C:/user_a_rsa.p12";
            signingToken = new Pkcs12SignatureToken("password", pkcs12TokenFile);
            privateKey = signingToken.getKeys().get(0);*/

    }

    public AbstractSignatureTokenConnection getSigningToken() {
        return signingToken;
    }

    public void setSigningToken(AbstractSignatureTokenConnection signingToken) {
        this.signingToken = signingToken;
    }

    public DSSPrivateKeyEntry getPrivateKey() {
        return privateKey;
    }

    public void setPrivateKey(DSSPrivateKeyEntry privateKey) {
        this.privateKey = privateKey;
    }






}
