package org.mipams.provenance.demo.entities.responses;

import java.util.List;

import org.mipams.provenance.entities.assertions.Assertion;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@ToString
@NoArgsConstructor
public class FakeMediaResponse {
    private @Getter @Setter String manifestId;
    private @Getter @Setter boolean isManifestProtected = false;
    private @Getter @Setter List<Assertion> assertionList;
    private @Getter @Setter ClaimResponse claimResponse;
    private @Getter @Setter List<String> inaccessibleJumbfBoxLabelList;

}
