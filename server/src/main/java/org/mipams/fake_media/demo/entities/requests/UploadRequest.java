package org.mipams.fake_media.demo.entities.requests;

import org.springframework.web.multipart.MultipartFile;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.ToString;

@ToString
public class UploadRequest {
    private @Getter @Setter @NonNull MultipartFile file;
}