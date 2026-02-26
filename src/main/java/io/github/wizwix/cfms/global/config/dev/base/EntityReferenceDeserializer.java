package io.github.wizwix.cfms.global.config.dev.base;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonMappingException;
import lombok.RequiredArgsConstructor;

import java.io.IOException;
import java.util.function.Function;

@RequiredArgsConstructor
public class EntityReferenceDeserializer<T> extends JsonDeserializer<T> {
  private final Function<String, T> lookupFunction;

  @Override
  public T deserialize(JsonParser p, DeserializationContext ctx) throws IOException {
    if (!p.hasToken(JsonToken.VALUE_STRING)) {
      //noinspection unchecked
      return (T) ctx.reportInputMismatch(handledType(), "Expected a string ID for entity lookup, but found %s", p.currentToken());
    }

    String identifier = p.getText();

    try {
      T result = lookupFunction.apply(identifier);
      if (result == null) {
        throw new RuntimeException("Entity with ID [" + identifier + "] not found in DB.");
      }
      return result;
    } catch (Exception e) {
      throw JsonMappingException.from(p, "Error during entity lookup for ID: " + identifier, e);
    }
  }
}
