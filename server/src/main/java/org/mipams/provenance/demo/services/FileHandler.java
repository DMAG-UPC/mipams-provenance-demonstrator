package org.mipams.provenance.demo.services;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import org.mipams.jumbf.util.CoreUtils;
import org.mipams.jumbf.util.MipamsException;
import org.mipams.provenance.demo.entities.requests.UploadRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.MalformedURLException;

import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

@Service
public class FileHandler {

    @Value("${org.mipams.provenance.demo.working_dir}")
    String FAKE_MEDIA_WORKING_DIRECTORY;

    public String saveFileToDiskAndGetFileUrl(UploadRequest request, boolean randomTargetName) throws MipamsException {

        String targetFileName = randomTargetName ? CoreUtils.randomStringGenerator()
                : request.getFile().getOriginalFilename();

        String targetFilePath = CoreUtils.getFullPath(FAKE_MEDIA_WORKING_DIRECTORY, targetFileName);

        File targetFile = new File(targetFilePath);

        try {
            request.getFile().transferTo(targetFile);
            return targetFilePath;
        } catch (IllegalStateException | IOException e) {
            throw new MipamsException("Could not upload file", e);
        }
    }

    public String getFileUrl(String fileName) {
        return CoreUtils.getFullPath(FAKE_MEDIA_WORKING_DIRECTORY, fileName);
    }

    public String saveContentToDiskAndGetFileUrl(byte[] content) throws MipamsException {

        String targetFilePath = CoreUtils.getFullPath(FAKE_MEDIA_WORKING_DIRECTORY, CoreUtils.randomStringGenerator());

        try (FileOutputStream outputStream = new FileOutputStream(targetFilePath)) {
            outputStream.write(content);
        } catch (IOException e) {
            throw new MipamsException(e);
        }

        return targetFilePath;
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
        String fileUrl = CoreUtils.getFullPath(FAKE_MEDIA_WORKING_DIRECTORY, digitalAssetFileId);

        File f = new File(fileUrl);

        if (!f.exists()) {
            throw new MipamsException("Could not locate file with id: " + digitalAssetFileId);
        }

        return fileUrl;
    }

}
