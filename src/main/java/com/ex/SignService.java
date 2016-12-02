package com.ex;

import com.google.common.io.Files;
import eu.europa.esig.dss.*;
import eu.europa.esig.dss.client.tsp.OnlineTSPSource;
import eu.europa.esig.dss.pades.PAdESSignatureParameters;
import eu.europa.esig.dss.pades.signature.PAdESService;
import eu.europa.esig.dss.validation.CommonCertificateVerifier;
import eu.europa.esig.dss.xades.XAdESSignatureParameters;
import eu.europa.esig.dss.xades.signature.XAdESService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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

    @Value(value = "${folder.path}")
    private String FOLDER_PATH;

    @Autowired
    private KeyStoreService keyStoreService;

    private Set<String> signedFiles = new HashSet<>();

    @Scheduled(fixedDelay=15000)
    public void signFiles() throws IOException {
        for(File file : listFilesForFolder(new File(FOLDER_PATH))) {
            String fileName = file.getName();
            String extension = Files.getFileExtension(fileName);

           /* int index = file.getName().lastIndexOf(".");
            String ext = file.getName().substring(index+1);*/

            if(signedFiles.contains(file.getName()) || extension.equals("pades") || extension.equals("xades"))
              continue;

            if(extension.equals("pdf")) {
                signPDF(file);
                signedFiles.add(fileName);
            }

            if(extension.equals("xml")) {
                signXML(file);
                signedFiles.add(fileName);
            }

        }
    }

    private File[] listFilesForFolder(final File folder) {
       return folder.listFiles();
    }

    private void signXML(File file) throws IOException {
        DSSDocument toSignDocument = new FileDocument(file);

        XAdESSignatureParameters parameters = new XAdESSignatureParameters ();
        parameters.setSignatureLevel(SignatureLevel.XAdES_BASELINE_T);
        parameters.setSignaturePackaging(SignaturePackaging.ENVELOPED); // signature part of PDF
        parameters.setDigestAlgorithm(DigestAlgorithm.SHA256);
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

    private void signPDF(File file) throws IOException {
        DSSDocument toSignDocument = new FileDocument(file);

        PAdESSignatureParameters parameters = new PAdESSignatureParameters ();
        parameters.setSignatureLevel(SignatureLevel.PAdES_BASELINE_T);
        parameters.setSignaturePackaging(SignaturePackaging.ENVELOPED); // signature part of PDF
        parameters.setDigestAlgorithm(DigestAlgorithm.SHA256);
        parameters.setSigningCertificate(keyStoreService.getPrivateKey().getCertificate());
        parameters.setCertificateChain(keyStoreService.getPrivateKey().getCertificateChain());

        // For LT-level signatures, we would need a TrustedListCertificateVerifier, but for level T,
        // a CommonCertificateVerifier is enough. (CookBook v 2.2 pg 28)
        CommonCertificateVerifier commonCertificateVerifier = new CommonCertificateVerifier();
        PAdESService service = new PAdESService (commonCertificateVerifier);

        OnlineTSPSource tspSource = new OnlineTSPSource("http://tsa.belgium.be/connect");
        service.setTspSource(tspSource);

        SignatureValue signatureValue = keyStoreService.getSigningToken().sign(service.getDataToSign(toSignDocument, parameters),
                parameters.getDigestAlgorithm(),
                keyStoreService.getPrivateKey());

        DSSDocument signedDocument = service.signDocument(toSignDocument, parameters, signatureValue);


        signedDocument.save(FOLDER_PATH + file.getName() + ".pades");
    }

}
