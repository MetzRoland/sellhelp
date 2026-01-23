package org.sellhelp.backend.dtos.validationGroups;

import jakarta.validation.GroupSequence;

@GroupSequence({
        NotBlankGroup.class,
        SizeGroup.class,
        PatternGroup.class,
        PastGroup.class
})
public interface ValidationOrder {
}
