package org.odata4j.format.json;

import java.io.Reader;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.SerializationUtils;
import org.odata4j.core.ODataConstants;
import org.odata4j.core.OEntities;
import org.odata4j.core.OProperties;
import org.odata4j.core.OProperty;
import org.odata4j.edm.EdmEntitySet;
import org.odata4j.exceptions.NotFoundException;
import org.odata4j.format.Entry;
import org.odata4j.format.FormatParser;
import org.odata4j.format.Settings;
import org.odata4j.format.json.JsonFeedFormatParser.JsonEntry;
import org.odata4j.format.json.JsonStreamReaderFactory.JsonStreamReader;

import com.google.gson.Gson;

public class JsonEntryFormatParser extends JsonFormatParser implements FormatParser<Entry> {

	public JsonEntryFormatParser(Settings settings) {
		super(settings);
	}

	@Override
	public Entry parse(Reader reader) {

		Class<? extends Serializable> clazz = metadata.findJavaClass(entitySetName);

		if (clazz != null) {
			EdmEntitySet entitySet = metadata.findEdmEntitySet(entitySetName);
			if (entitySet == null) {
				throw new NotFoundException();
			}
			Gson gson = new Gson();
			Serializable parsedObject = gson.fromJson(reader, clazz);
			byte[] data = SerializationUtils.serialize(parsedObject);
			OProperty<?> property = OProperties.binary(ODataConstants.GSON_PARSED, data);
			List<OProperty<?>> properties = new ArrayList<OProperty<?>>(1);
			properties.add(property);
			JsonEntry entry = new JsonEntry(null, null);
			entry.oentity = OEntities.create(entitySet, entityKey, properties, null);
			return entry;
		}

    JsonStreamReader jsr = JsonStreamReaderFactory.createJsonStreamReader(reader);
    try {
      ensureNext(jsr);

      // skip the StartObject event
      ensureStartObject(jsr.nextEvent());

      if (isResponse) {
        // "d" property
        ensureNext(jsr);
        ensureStartProperty(jsr.nextEvent(), DATA_PROPERTY);

        // skip the StartObject event
        ensureStartObject(jsr.nextEvent());
      }

      // parse the entry
      return parseEntry(metadata.getEdmEntitySet(entitySetName), jsr);

      // no interest in the closing events
    } finally {
      jsr.close();
    }
  }

}
