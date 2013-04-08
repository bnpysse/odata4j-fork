package org.odata4j.format.json;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;
import static org.odata4j.format.json.JsonFormatConstants.METADATA_PROPERTY;
import static org.odata4j.format.json.JsonFormatConstants.TYPE_PROPERTY;
import static org.odata4j.format.json.JsonFormatConstants.URI_PROPERTY;

import java.io.Reader;

import org.odata4j.core.OCollection;
import org.odata4j.core.OEntities;
import org.odata4j.core.OEntity;
import org.odata4j.core.OEntityKey;
import org.odata4j.core.OObject;
import org.odata4j.core.OProperties;
import org.odata4j.core.OProperty;
import org.odata4j.edm.EdmCollectionType;
import org.odata4j.edm.EdmDataServices;
import org.odata4j.edm.EdmEntityType;
import org.odata4j.edm.EdmProperty;
import org.odata4j.edm.EdmSimpleType;
import org.odata4j.edm.EdmType;
import org.odata4j.exceptions.NotFoundException;
import org.odata4j.format.Entry;
import org.odata4j.format.FormatParser;
import org.odata4j.format.FormatParserContext;
import org.odata4j.format.Settings;
import org.odata4j.format.json.JsonStreamReaderFactory.JsonStreamTokenizer.JsonTokenType;

import com.google.common.collect.ImmutableList;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;

public class GsonEntryParser implements GsonParser<Entry>, FormatParser<Entry> {

  private final FormatParserContext instanceContext;
  private final JsonParser parser = new JsonParser();

  public GsonEntryParser(Settings settings) {
    instanceContext = FormatParserContext.fromSettings(settings);
  }

  @Override
  public Entry parse(Reader reader) {
    checkState(instanceContext != null, "parse(Reader) can only be called on a parser instance that has been initialized with Settings.");
    return parser(reader, instanceContext);
  }

  @Override
  public Entry parser(Reader reader, FormatParserContext context) {
    return parser(parser.parse(reader), context);
  }

  @Override
  public Entry parser(JsonElement json, FormatParserContext context) {
    EdmType parseType = context.getParseType();
    checkArgument(parseType instanceof EdmEntityType, "Requested parse type should be instance of EdmEntityType but is %s", parseType.getClass().getName());

    JsonObject entryJson = json.getAsJsonObject();
    JsonObject metadata = entryJson.getAsJsonObject(METADATA_PROPERTY);
    final EdmEntityType entityType = resolveEntityType(metadata, context.getMetadata(), (EdmEntityType) parseType);
    final String uri = getUri(metadata);

    ImmutableList.Builder<OProperty<?>> propertiesBuilder = ImmutableList.builder();
    for (EdmProperty property : entityType.getProperties()) {
      EdmType propertyType = property.getType();
      String propertyName = property.getName();
      JsonElement propertyValueJson = entryJson.get(propertyName);

      OProperty<?> oProperty = parseProperty(propertyValueJson, propertyName, propertyType);
      if (oProperty != null) {
        propertiesBuilder.add(oProperty);
      }
    }

    if (entityType.getOpenType()) {
      // TODO: parse open properties
    } else {
      // TODO: check that there a no unknown properties specified
    }

    // TODO: parse nav properties

    ImmutableList<OProperty<?>> properties = propertiesBuilder.build();
    OEntityKey entityKey = context.getEntityKey() == null ? OEntityKey.infer(context.getEntitySet(), properties) : context.getEntityKey();
    final OEntity entity = OEntities.create(context.getEntitySet(), entityType, entityKey, properties, null);

    return new Entry() {

      @Override
      public String getUri() {
        return uri;
      }

      @Override
      public OEntity getEntity() {
        return entity;
      }
    };
  }

  private EdmEntityType resolveEntityType(JsonObject entityMetadata, EdmDataServices metadata, EdmEntityType requestedEntityType) {
    if (entityMetadata == null) {
      return requestedEntityType;
    }

    JsonPrimitive typeJson = entityMetadata.getAsJsonPrimitive(TYPE_PROPERTY);
    if (typeJson == null) {
      return requestedEntityType;
    }

    String fqTypeName = typeJson.getAsString();
    EdmEntityType entityType = (EdmEntityType) metadata.findEdmEntityType(fqTypeName);
    if (entityType == null) {
      throw new NotFoundException(String.format("Entity type %s specified in the entry metadata was not found.", fqTypeName));
    }

    return entityType;
  }

  private String getUri(JsonObject metadata) {
    JsonPrimitive uriJson = metadata.getAsJsonPrimitive(URI_PROPERTY);
    return uriJson == null ? null : uriJson.getAsString();
  }

  private OProperty<?> parseProperty(JsonElement propertyValueJson, String propertyName, EdmType propertyType) {
    if (propertyValueJson == null) {
      return null;
    }

    if (propertyType instanceof EdmCollectionType) {
      checkNotNull(propertyValueJson, "Collection value cannot be null.");
      EdmCollectionType collectionType = (EdmCollectionType) propertyType;
      OCollection<? extends OObject> collection = parseCollection(propertyValueJson, collectionType.getItemType());
      return OProperties.collection(propertyName, (EdmCollectionType) propertyType, collection);
    }
    if (propertyType.isSimple()) {
      return parseSimpleProperty(propertyValueJson, propertyName, (EdmSimpleType<?>) propertyType);
    }else {
      // TODO: complex object
      return null;
    }
  }

  private OCollection<? extends OObject> parseCollection(JsonElement propertyValueJson, EdmType itemType) {
    // TODO Auto-generated method stub
    return null;
  }

  private OProperty<?> parseSimpleProperty(JsonElement propertyValueJson, String propertyName, EdmSimpleType<?> propertyType) {
    if (propertyValueJson == null) {
      return null;
    }
    if (propertyValueJson == JsonNull.INSTANCE) {
      return OProperties.null_(propertyName, propertyType);
    }

    String propertyValue = propertyValueJson.getAsJsonPrimitive().getAsString();
    JsonTokenType tokenType = propertyType == EdmSimpleType.BOOLEAN ? JsonTokenType.FALSE : JsonTokenType.STRING;

    return JsonTypeConverter.parse(propertyName, propertyType, propertyValue, tokenType);
  }

}
