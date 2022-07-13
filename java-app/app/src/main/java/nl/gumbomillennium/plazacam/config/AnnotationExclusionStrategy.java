package nl.gumbomillennium.plazacam.config;

import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;
import nl.gumbomillennium.plazacam.annotations.IgnoreFromJsonSerialisation;

public class AnnotationExclusionStrategy implements ExclusionStrategy {
  public boolean shouldSkipClass(Class<?> classDescription) {
    return classDescription.getAnnotation(IgnoreFromJsonSerialisation.class) != null;
  }

  public boolean shouldSkipField(FieldAttributes attributes) {
    return attributes.getAnnotation(IgnoreFromJsonSerialisation.class) != null;
  }
}
