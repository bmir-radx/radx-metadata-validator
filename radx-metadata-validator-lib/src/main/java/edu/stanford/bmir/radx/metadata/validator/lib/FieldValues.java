package edu.stanford.bmir.radx.metadata.validator.lib;

import java.net.URI;
import java.util.List;
import java.util.Optional;

/**
 * Represents the JSON object value for a field.  These JSON objects can represent IRIs (using @id) in which case
 * there can be an rdfs:label JSON field too.  They can also represent literals (using @value).  Examples:
 *
 *
 * {
 *      "@id" : "https://example.org/XYZ",
 *      "rdfs:label" : "Foot"
 * {
 *      "@id" : "https://example.org/PQR"
 * }
 *
 * {
 *       "@value" : "Hello world"
 * }
 *
 * {
 *       "@value" : "2025-07-08",
 *       "@type"  : "xsd:date"
 * }
 *
 *
 * @param jsonLdTypes The @type value
 * @param jsonLdId The @id value
 * @param jsonLdValue The @value value
 * @param label The rdfs:label value
 */
public record FieldValues(List<URI> jsonLdTypes, Optional<URI> jsonLdId, Optional<String> jsonLdValue,
                          Optional<String> label) {
}
