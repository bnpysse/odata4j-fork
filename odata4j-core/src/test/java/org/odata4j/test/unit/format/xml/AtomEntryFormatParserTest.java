package org.odata4j.test.unit.format.xml;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.io.StringReader;

import org.junit.BeforeClass;
import org.junit.Test;
import org.odata4j.core.Guid;
import org.odata4j.format.FormatType;
import org.odata4j.test.unit.format.AbstractEntryFormatParserTest;

public class AtomEntryFormatParserTest extends AbstractEntryFormatParserTest {

  @BeforeClass
  public static void setupClass() throws Exception {
    createFormatParser(FormatType.ATOM);
  }

  @Test
  public void dateTime() throws Exception {
    verifyDateTimePropertyValue(formatParser.parse(buildAtom("<d:DateTime m:type=\"Edm.DateTime\">2005-04-03T01:02</d:DateTime>")), DATETIME);
  }

  @Test
  public void dateTimeWithSeconds() throws Exception {
    verifyDateTimePropertyValue(formatParser.parse(buildAtom("<d:DateTime m:type=\"Edm.DateTime\">2006-05-04T01:02:03</d:DateTime>")), DATETIME_WITH_SECONDS);
  }

  @Test
  public void dateTimeWithMillis() throws Exception {
    verifyDateTimePropertyValue(formatParser.parse(buildAtom("<d:DateTime m:type=\"Edm.DateTime\">2007-06-05T01:02:03.004</d:DateTime>")), DATETIME_WITH_MILLIS);
    verifyDateTimePropertyValue(formatParser.parse(buildAtom("<d:DateTime m:type=\"Edm.DateTime\">2007-06-05T01:02:03.01</d:DateTime>")), DATETIME_WITH_MILLIS.withMillisOfSecond(10));
  }

  @Test
  public void dateTimeWithMillisRounded() throws Exception {
    verifyDateTimePropertyValue(formatParser.parse(buildAtom("<d:DateTime m:type=\"Edm.DateTime\">2007-06-05T01:02:03.0004</d:DateTime>")), DATETIME_WITH_MILLIS.withMillisOfSecond(0));
    verifyDateTimePropertyValue(formatParser.parse(buildAtom("<d:DateTime m:type=\"Edm.DateTime\">2007-06-05T01:02:03.0005</d:DateTime>")), DATETIME_WITH_MILLIS.withMillisOfSecond(1));
    verifyDateTimePropertyValue(formatParser.parse(buildAtom("<d:DateTime m:type=\"Edm.DateTime\">2007-06-05T01:02:03.0095</d:DateTime>")), DATETIME_WITH_MILLIS.withMillisOfSecond(10));
  }

  @Test(expected=IllegalArgumentException.class)
  public void illegalDateTime() throws Exception {
    formatParser.parse(buildAtom("<d:DateTime m:type=\"Edm.DateTime\">1969-08-07T05:06:00Z</d:DateTime>"));
  }

  @Test
  public void dateTimeNoOffset() throws Exception {
    verifyDateTimeOffsetPropertyValue(formatParser.parse(buildAtom("<d:DateTimeOffset m:type=\"Edm.DateTimeOffset\">1969-08-07T05:06:00Z</d:DateTimeOffset>")), DATETIME_BEFORE_1970_NO_OFFSET);
  }

  @Test
  public void dateTimeWithSecondsPositiveOffset() throws Exception {
    verifyDateTimeOffsetPropertyValue(formatParser.parse(buildAtom("<d:DateTimeOffset m:type=\"Edm.DateTimeOffset\">2006-05-04T01:02:03+07:00</d:DateTimeOffset>")), DATETIME_WITH_SECONDS_POSITIVE_OFFSET);
  }

  @Test
  public void dateTimeWithMillisNegativeOffset() throws Exception {
    verifyDateTimeOffsetPropertyValue(formatParser.parse(buildAtom("<d:DateTimeOffset m:type=\"Edm.DateTimeOffset\">2007-06-05T01:02:03.004-08:00</d:DateTimeOffset>")), DATETIME_WITH_MILLIS_NEGATIVE_OFFSET);
    verifyDateTimeOffsetPropertyValue(formatParser.parse(buildAtom("<d:DateTimeOffset m:type=\"Edm.DateTimeOffset\">2007-06-05T01:02:03.01-08:00</d:DateTimeOffset>")), DATETIME_WITH_MILLIS_NEGATIVE_OFFSET.withMillisOfSecond(10));
  }

  @Test
  public void dateTimeWithMillisNegativeOffsetRounded() throws Exception {
    verifyDateTimeOffsetPropertyValue(formatParser.parse(buildAtom("<d:DateTimeOffset m:type=\"Edm.DateTimeOffset\">2007-06-05T01:02:03.0004-08:00</d:DateTimeOffset>")), DATETIME_WITH_MILLIS_NEGATIVE_OFFSET.withMillisOfSecond(0));
    verifyDateTimeOffsetPropertyValue(formatParser.parse(buildAtom("<d:DateTimeOffset m:type=\"Edm.DateTimeOffset\">2007-06-05T01:02:03.0005-08:00</d:DateTimeOffset>")), DATETIME_WITH_MILLIS_NEGATIVE_OFFSET.withMillisOfSecond(1));
    verifyDateTimeOffsetPropertyValue(formatParser.parse(buildAtom("<d:DateTimeOffset m:type=\"Edm.DateTimeOffset\">2007-06-05T01:02:03.0095-08:00</d:DateTimeOffset>")), DATETIME_WITH_MILLIS_NEGATIVE_OFFSET.withMillisOfSecond(10));
  }

  @Test(expected=IllegalArgumentException.class)
  public void illegalDateTimeOffset() throws Exception {
    formatParser.parse(buildAtom("<d:DateTimeOffset m:type=\"Edm.DateTimeOffset\">2005-04-03T01:02</d:DateTimeOffset>"));
  }

  @Test
  public void time() throws Exception {
    verifyTimePropertyValue(formatParser.parse(buildAtom("<d:Time m:type=\"Edm.Time\">PT1H2M3S</d:Time>")), TIME);
  }

  @Test
  public void timeWithMillis() throws Exception {
    verifyTimePropertyValue(formatParser.parse(buildAtom("<d:Time m:type=\"Edm.Time\">PT1H2M3.004S</d:Time>")), TIME_WITH_MILLIS);
  }

  @Test(expected=IllegalArgumentException.class)
  public void illegalTime() throws Exception {
    formatParser.parse(buildAtom("<d:Time m:type=\"Edm.Time\">01:02:03</d:Time>"));
  }

  @Test
  public void bool() throws Exception {
    assertThat((Boolean) formatParser.parse(buildAtom("<d:Boolean m:type=\"Edm.Boolean\">true</d:Boolean>")).getEntity().getProperty(BOOLEAN_NAME).getValue(), is(BOOLEAN));
  }

  @Test(expected=IllegalArgumentException.class)
  public void illegalBoolean() throws Exception {
    formatParser.parse(buildAtom("<d:Boolean m:type=\"Edm.Boolean\">undefined</d:Boolean>"));
  }

  @Test
  public void string() throws Exception {
    assertThat((String) formatParser.parse(buildAtom("<d:String>&lt;\"\t€\"&gt;</d:String>")).getEntity().getProperty(STRING_NAME).getValue(), is(STRING));
  }

  @Test
  public void guid() throws Exception {
    assertThat((Guid) formatParser.parse(buildAtom("<d:Guid m:type=\"Edm.Guid\">4786c33c-1e3d-4b57-b5cf-a4b759acac44</d:Guid>")).getEntity().getProperty(GUID_NAME).getValue(), is(GUID));
  }

  @Test(expected=IllegalArgumentException.class)
  public void illegalGuid() throws Exception {
    formatParser.parse(buildAtom("<d:Guid m:type=\"Edm.Guid\">a-b-c-d</d:Guid>"));
  }

  private StringReader buildAtom(String property) {
    return new StringReader("" +
        "<entry" +
        " xmlns=\"http://www.w3.org/2005/Atom\"" +
        " xmlns:m=\"http://schemas.microsoft.com/ado/2007/08/dataservices/metadata\"" +
        " xmlns:d=\"http://schemas.microsoft.com/ado/2007/08/dataservices\">" +
        "<content type=\"application/xml\">" +
        "<m:properties>" + property + "</m:properties>" +
        "</content>" +
        "</entry>");
  }
}
