package org.mipams.provenance.demo.entities.responses;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
public class ClaimResponse {
    private @Getter @Setter String signedBy;
    private @Getter @Setter String signedOn;
    private @Getter @Setter String signedGeneratorDescription;
}
