package com.ex;

import com.j256.simplemagic.ContentInfo;
import com.j256.simplemagic.ContentInfoUtil;
import com.j256.simplemagic.ContentType;
import eu.europa.esig.dss.*;
import eu.europa.esig.dss.client.tsp.OnlineTSPSource;
import eu.europa.esig.dss.pades.PAdESSignatureParameters;
import eu.europa.esig.dss.pades.signature.PAdESService;
import eu.europa.esig.dss.validation.CommonCertificateVerifier;
import eu.europa.esig.dss.xades.XAdESSignatureParameters;
import eu.europa.esig.dss.xades.signature.XAdESService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by Bartek on 29.11.2016.
 */
@Service
public class SignService {

    private static final String FOLDER_PATH = "C:/podpis/";

    @Autowired
    private KeyStoreService keyStoreService;

    private Set<String> signedFiles = new HashSet<>();

    public void sign(File file) throws IOException {
        DSSDocument toSignDocument = new FileDocument(file);

        XAdESSignatureParameters parameters = new XAdESSignatureParameters ();
        parameters.setSignatureLevel(SignatureLevel.XAdES_BASELINE_B);
        parameters.setSignaturePackaging(SignaturePackaging.ENVELOPING); // signature part of PDF
        parameters.setDigestAlgorithm(DigestAlgorithm.SHA1);
        parameters.setSigningCertificate(keyStoreService.getPrivateKey().getCertificate());
        parameters.setCertificateChain(keyStoreService.getPrivateKey().getCertificateChain());

        // For LT-level signatures, we would need a TrustedListCertificateVerifier, but for level T,
        // a CommonCertificateVerifier is enough. (CookBook v 2.2 pg 28)
        CommonCertificateVerifier commonCertificateVerifier = new CommonCertificateVerifier();
        XAdESService service = new XAdESService (commonCertificateVerifier);

        // For now, just hard-code one specific time stamp server (the same as DSS demo app uses by default)
        OnlineTSPSource tspSource = new OnlineTSPSource("http://tsa.belgium.be/connect");
        service.setTspSource(tspSource);

        SignatureValue signatureValue = keyStoreService.getSigningToken().sign(service.getDataToSign(toSignDocument, parameters),
                parameters.getDigestAlgorithm(),
                keyStoreService.getPrivateKey());

        DSSDocument signedDocument = service.signDocument(toSignDocument, parameters, signatureValue);


        signedDocument.save(FOLDER_PATH + file.getName() + ".xades");


    }

    @Scheduled(fixedDelay=15000)
    public void signFiles() throws IOException {
        for(File file : listFilesForFolder(new File(FOLDER_PATH))) {
            int index = file.getName().lastIndexOf(".");
            String ext = file.getName().substring(index+1);
            if(!signedFiles.contains(file.getName()) && !ext.equals("xades"))
                sign(file);
                signedFiles.add(file.getName());
        }
    }

    private File[] listFilesForFolder(final File folder) {
       return folder.listFiles();
    }

}
