package edu.stanford.bmir.radx.metadata.validator.lib;

import java.net.URI;
import java.util.List;
import java.util.Optional;

public record FieldValues(List<URI> jsonLdTypes, Optional<URI> jsonLdId, Optional<String> jsonLdValue,
                          Optional<String> label) {
}
