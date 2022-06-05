package org.mipams.fake_media.demo.entities.responses;

import java.util.List;

import org.mipams.fake_media.entities.assertions.Assertion;

import lombok.AllArgsConstructor;
import lombok.Getter;

import lombok.ToString;

@ToString
@AllArgsConstructor
public class ProvenanceResponse {
    private @Getter String digitalAssetUrl;
    private @Getter List<Assertion> assertionList;
}
