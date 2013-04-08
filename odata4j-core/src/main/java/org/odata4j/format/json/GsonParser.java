package org.odata4j.format.json;

import org.odata4j.format.ContextualFormatParser;
import org.odata4j.format.FormatParserContext;

import com.google.gson.JsonElement;

public interface GsonParser<T> extends ContextualFormatParser<T> {

  T parser(JsonElement json, FormatParserContext context);

}
