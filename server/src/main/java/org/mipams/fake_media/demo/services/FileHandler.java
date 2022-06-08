package org.mipams.fake_media.demo.services;

import java.io.File;
import java.io.IOException;

import org.mipams.fake_media.demo.entities.requests.UploadRequest;
import org.mipams.jumbf.core.util.CoreUtils;
import org.mipams.jumbf.core.util.MipamsException;
import org.mipams.jumbf.core.util.Properties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.net.MalformedURLException;

import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

@Service
public class FileHandler {

    @Autowired
    Properties properties;

    public String saveFileToDiskAndGetFileUrl(UploadRequest request, boolean randomTargetName) throws MipamsException {

        String targetFileName = randomTargetName ? CoreUtils.randomStringGenerator()
                : request.getFile().getOriginalFilename();

        String targetFilePath = CoreUtils.getFullPath(properties.getFileDirectory(), targetFileName);

        File targetFile = new File(targetFilePath);

        try {
            request.getFile().transferTo(targetFile);
            return targetFilePath;
        } catch (IllegalStateException | IOException e) {
            throw new MipamsException("Could not upload file", e);
        }
    }

    public ResponseEntity<?> createOctetResponse(String fileUrl) throws MipamsException {
        UrlResource urlResource;

        try {
            urlResource = new UrlResource("file:" + fileUrl);
        } catch (MalformedURLException e) {
            throw new MipamsException("The file path is malformed", e);
        }

        StringBuilder headerValue = new StringBuilder("attachment; filename=\"").append(fileUrl).append("\"");
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION, headerValue.toString())
                .body(urlResource);
    }

    public String checkFileNameExistanceAndGetFileUrl(String digitalAssetFileId) throws MipamsException {
        String fileUrl = CoreUtils.getFullPath(properties.getFileDirectory(), digitalAssetFileId);

        File f = new File(fileUrl);

        if (!f.exists()) {
            throw new MipamsException("Could not locate file with id: " + digitalAssetFileId);
        }

        return fileUrl;
    }

}
