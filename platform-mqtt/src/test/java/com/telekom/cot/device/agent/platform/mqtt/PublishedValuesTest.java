package com.telekom.cot.device.agent.platform.mqtt;

import static org.junit.Assert.assertThat;

import org.hamcrest.Matchers;
import org.junit.Test;

public class PublishedValuesTest {

    @Test
    public void testPublishedValues() {
        PublishedValues publishedValues = new PublishedValues(TemplateId.CREATE_ALARM_REQ,
                        new String[] { "attr0", "attr1" }, new String[] { "one", "two" });
        assertThat(publishedValues.size(), Matchers.equalTo(2));
        assertThat(publishedValues.isValid(), Matchers.equalTo(true));
        assertThat(publishedValues.getValue("attr0"), Matchers.equalTo("one"));
        assertThat(publishedValues.getValue("attr1"), Matchers.equalTo("two"));
    }

    @Test
    public void testPublishedValuesError() {
        PublishedValues publishedValues = new PublishedValues(TemplateId.CREATE_ALARM_REQ,
                        new String[] { "attr0" },new String[] { "one", "two" });
        assertThat(publishedValues.size(), Matchers.equalTo(2));
        assertThat(publishedValues.isValid(), Matchers.equalTo(false));
    }
}
