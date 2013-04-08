package org.odata4j.format;

import java.io.Reader;

public interface ContextualFormatParser<T> {

  T parser(Reader reader, FormatParserContext context);

}
