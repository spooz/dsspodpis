package com.ex;

import com.google.common.io.Files;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * Created by spooz on 24.12.2016.
 */
@Component
public class DbFileChecker {

    private static final Logger log = LoggerFactory.getLogger(DbFileChecker.class);


    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private SignService signService;


    @Scheduled(fixedDelay = 10000)
    public void checkDB() throws IOException {
        List<SignFile> files = jdbcTemplate.query(
                "SELECT promotional_material_id, filename, modificationdate, company_id, filecontent FROM promotional_material",
                (rs, rowNum) -> new SignFile(rs.getLong("promotional_material_id"), rs.getString("filename"),  rs.getLong("company_id"),
                        rs.getDate("modificationdate"), rs.getBytes("filecontent")));
        for(SignFile file : files) {
            String extension = Files.getFileExtension(file.getFilename());
            if(signService.signedFiles.contains(file.getFilename()) || extension.equals("xades") || extension.equals("pades"))
                continue;
            log.info("Saving file " + file.getFilename());
            FileUtils.writeByteArrayToFile(new File(signService.FOLDER_PATH + file.getFilename()), file.getData());

        }

    }



}
