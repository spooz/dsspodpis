package com.ex;

import com.j256.simplemagic.ContentInfo;
import com.j256.simplemagic.ContentInfoUtil;
import com.j256.simplemagic.ContentType;
import eu.europa.esig.dss.*;
import eu.europa.esig.dss.client.tsp.OnlineTSPSource;
import eu.europa.esig.dss.pades.PAdESSignatureParameters;
import eu.europa.esig.dss.pades.signature.PAdESService;
import eu.europa.esig.dss.validation.CommonCertificateVerifier;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;

/**
 * Created by Bartek on 29.11.2016.
 */
@Service
public class SignService {

    private static final String FOLDER_PATH = "F:/klucze/";
    private static final String FOLDER_PATH_PADES = "F:/klucze/pades/";

    @Autowired
    private KeyStoreService keyStoreService;

    public void sign(File file) throws IOException {
        DSSDocument toSignDocument = new FileDocument(file);

        PAdESSignatureParameters parameters = new PAdESSignatureParameters();
        parameters.setSignatureLevel(SignatureLevel.PAdES_BASELINE_T);
        parameters.setSignaturePackaging(SignaturePackaging.ENVELOPED); // signature part of PDF
        parameters.setDigestAlgorithm(DigestAlgorithm.SHA256);
        parameters.setSigningCertificate(keyStoreService.getPrivateKey().getCertificate());

        // For LT-level signatures, we would need a TrustedListCertificateVerifier, but for level T,
        // a CommonCertificateVerifier is enough. (CookBook v 2.2 pg 28)
        CommonCertificateVerifier commonCertificateVerifier = new CommonCertificateVerifier();
        PAdESService service = new PAdESService(commonCertificateVerifier);

        // For now, just hard-code one specific time stamp server (the same as DSS demo app uses by default)
        OnlineTSPSource tspSource = new OnlineTSPSource("http://tsa.belgium.be/connect");
        service.setTspSource(tspSource);

        SignatureValue signatureValue = keyStoreService.getSigningToken().sign(service.getDataToSign(toSignDocument, parameters),
                parameters.getDigestAlgorithm(),
                keyStoreService.getPrivateKey());

        DSSDocument signedDocument = service.signDocument(toSignDocument, parameters, signatureValue);

        int index = file.getName().indexOf(".");
        String name = file.getName().substring(0, index);


        signedDocument.save(FOLDER_PATH_PADES + name + ".pades");
    }

    @Scheduled(fixedDelay=15000)
    public void signFiles() throws IOException {
        for(File file : listFilesForFolder(new File(FOLDER_PATH))) {
            ContentInfoUtil util = new ContentInfoUtil();
            ContentInfo info = util.findMatch(file);
            if(info != null && info.getContentType().equals(ContentType.PDF)) {

                sign(file);
                file.delete();
            }
        }
    }

    private File[] listFilesForFolder(final File folder) {
       return folder.listFiles();
    }

}
