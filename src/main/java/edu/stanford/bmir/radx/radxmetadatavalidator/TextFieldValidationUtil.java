package edu.stanford.bmir.radx.radxmetadatavalidator;

import org.metadatacenter.artifacts.model.core.fields.constraints.LiteralValueConstraint;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class TextFieldValidationUtil {
  public boolean matchRegex(Optional<String> regex, String textValueString){
    if(regex.isPresent()){
      String regexString = regex.get();
      return textValueString.matches(regexString);
    }else{
      return true;
    }
  }
  public boolean lengthIsInRange(Optional<Integer> minLength, Optional<Integer> maxLength, Integer length){
    return minLength.map(min -> length >= min).orElse(true)
        && maxLength.map(max -> length <= max).orElse(true);
  }

  public boolean validLiteral(List<LiteralValueConstraint> literals, String value) {
    for (var literal : literals) {
      if (literal.label().equals(value)) {
        return true;
      }
    }
    return false;
  }
}
