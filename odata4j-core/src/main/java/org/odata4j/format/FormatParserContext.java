package org.odata4j.format;

import static com.google.common.base.Preconditions.checkNotNull;

import org.odata4j.core.ODataVersion;
import org.odata4j.core.OEntityKey;
import org.odata4j.edm.EdmDataServices;
import org.odata4j.edm.EdmEntitySet;
import org.odata4j.edm.EdmType;
import org.odata4j.exceptions.NotFoundException;

public class FormatParserContext {
  private final ODataVersion version;
  private final EdmDataServices metadata;
  private final EdmEntitySet entitySet;
  private final OEntityKey entityKey;
  private final boolean isResponse;
  private final EdmType parseType;

  public static FormatParserContext fromSettings(Settings settings) {
    checkNotNull(settings);
    checkNotNull(settings.metadata, "Metadata is required.");

    EdmEntitySet entitySet = null;
    if (settings.entitySetName != null) {
      entitySet = settings.metadata.findEdmEntitySet(settings.entitySetName);
      if (entitySet == null) {
        throw new NotFoundException(String.format("Entity set '%s' was not found.", settings.entitySetName));
      }
    }

    EdmType parseType = null;
    if (settings.parseType != null) {
      parseType = settings.parseType;
    } else {
      checkNotNull(entitySet, "Entity set name is required when no parse type is specified.");
      parseType = entitySet.getType();
    }
    checkNotNull(parseType, "Parse type is required.");

    return new FormatParserContext(settings.version, settings.metadata, entitySet, settings.entityKey, settings.isResponse, parseType);
  }

  public FormatParserContext(ODataVersion version, EdmDataServices metadata, EdmEntitySet entitySet, OEntityKey entityKey, boolean isResponse, EdmType parseType) {
    this.version = version;
    this.metadata = checkNotNull(metadata);
    this.entitySet = entitySet;
    this.entityKey = entityKey;
    this.isResponse = isResponse;
    this.parseType = checkNotNull(parseType);
  }

  public ODataVersion getVersion() {
    return version;
  }

  public EdmDataServices getMetadata() {
    return metadata;
  }

  public EdmEntitySet getEntitySet() {
    return entitySet;
  }

  public OEntityKey getEntityKey() {
    return entityKey;
  }

  public boolean isResponse() {
    return isResponse;
  }

  public EdmType getParseType() {
    return parseType;
  }

  public static Builder builder() {
    return new Builder();
  }

  public static Builder builder(FormatParserContext templateContext) {
    return new Builder(templateContext);
  }

  public static class Builder {
    public ODataVersion version;
    public EdmDataServices metadata;
    public EdmEntitySet entitySet;
    public OEntityKey entityKey;
    public boolean isResponse;
    public EdmType parseType;

    public Builder() {}

    public Builder(FormatParserContext templateContext) {
      version = templateContext.version;
      metadata = templateContext.metadata;
      entitySet = templateContext.entitySet;
      entityKey = templateContext.entityKey;
      isResponse = templateContext.isResponse;
      parseType = templateContext.parseType;
    }

    public Builder version(ODataVersion version) {
      this.version = version;
      return this;
    }

    public Builder metadata(EdmDataServices metadata) {
      this.metadata = metadata;
      return this;
    }

    public Builder entitySet(EdmEntitySet entitySet) {
      this.entitySet = entitySet;
      return this;
    }

    public Builder entityKey(OEntityKey entityKey) {
      this.entityKey = entityKey;
      return this;
    }

    public Builder isResponse(boolean isResponse) {
      this.isResponse = isResponse;
      return this;
    }

    public Builder parseType(EdmType parseType) {
      this.parseType = parseType;
      return this;
    }

    public FormatParserContext build() {
      return new FormatParserContext(version, metadata, entitySet, entityKey, isResponse, parseType);
    }
  }

}
