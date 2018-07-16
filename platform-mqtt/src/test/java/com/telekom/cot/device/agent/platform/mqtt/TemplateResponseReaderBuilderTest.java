package com.telekom.cot.device.agent.platform.mqtt;

import static org.junit.Assert.assertThat;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import org.hamcrest.Matchers;
import org.junit.Test;

public class TemplateResponseReaderBuilderTest {

    @Test
    public void testRead() throws IOException, URISyntaxException {
        // response templates uri
        URI uri = TemplateResponseReaderBuilderTest.class.getResource("/templates/mqtt.response.templates").toURI();
        // create a reader based on response templates
        TemplateResponseReader reader = TemplateResponseReaderBuilder.getInstance().setTemplateResource(uri).build();
        // use reader
        String response = "511,2,19330625,EXECUTING,SUCCESSFUL";
        PublishedValues publishedValues = reader.read(response);
        // check
        assertThat(publishedValues.getValue("id"), Matchers.equalTo("19330625"));
        assertThat(publishedValues.getValue("status"), Matchers.equalTo("EXECUTING"));
        assertThat(publishedValues.getValue("c8y_TestOperation.givenStatus"), Matchers.equalTo("SUCCESSFUL"));
        assertThat(publishedValues.isValid(), Matchers.equalTo(true));
    }

    @Test
    public void testReadUnvalid() throws IOException, URISyntaxException {
        // response templates uri
        URI uri = TemplateResponseReaderBuilderTest.class.getResource("/templates/mqtt.response.templates").toURI();
        // create a reader based on response templates
        TemplateResponseReader reader = TemplateResponseReaderBuilder.getInstance().setTemplateResource(uri).build();
        // use reader
        String response = "511,2,19330625,EXECUTING,SUCCESSFUL,newvalue";
        PublishedValues publishedValues = reader.read(response);
        // check
        assertThat(publishedValues.getValue("id"), Matchers.equalTo("19330625"));
        assertThat(publishedValues.getValue("status"), Matchers.equalTo("EXECUTING"));
        assertThat(publishedValues.getValue("c8y_TestOperation.givenStatus"), Matchers.equalTo("SUCCESSFUL"));
        assertThat(publishedValues.getValue("unknown3"), Matchers.equalTo("newvalue"));
        assertThat(publishedValues.isValid(), Matchers.equalTo(false));
    }

    @Test
    public void testReadEmpty() throws IOException, URISyntaxException {
        // response templates uri
        URI uri = TemplateResponseReaderBuilderTest.class.getResource("/templates/mqtt.response.templates").toURI();
        // create a reader based on response templates
        TemplateResponseReader reader = TemplateResponseReaderBuilder.getInstance().setTemplateResource(uri).build();
        // use reader
        String response = "";
        PublishedValues publishedValues = reader.read(response);
        // check
        assertThat(publishedValues.getTemplateId(), Matchers.equalTo(TemplateId.NOT_AVAILABLE));
        assertThat(publishedValues.size(), Matchers.equalTo(0));
        assertThat(publishedValues.isValid(), Matchers.equalTo(false));
    }
    
    @Test
    public void testReadNull() throws IOException, URISyntaxException {
        // response templates uri
        URI uri = TemplateResponseReaderBuilderTest.class.getResource("/templates/mqtt.response.templates").toURI();
        // create a reader based on response templates
        TemplateResponseReader reader = TemplateResponseReaderBuilder.getInstance().setTemplateResource(uri).build();
        // use reader
        String response = null;
        PublishedValues publishedValues = reader.read(response);
        // check
        assertThat(publishedValues.getTemplateId(), Matchers.equalTo(TemplateId.NOT_AVAILABLE));
        assertThat(publishedValues.size(), Matchers.equalTo(0));
        assertThat(publishedValues.isValid(), Matchers.equalTo(false));
    }
    
    @Test
    public void testReadNoValues() throws IOException, URISyntaxException {
        // response templates uri
        URI uri = TemplateResponseReaderBuilderTest.class.getResource("/templates/mqtt.response.templates").toURI();
        // create a reader based on response templates
        TemplateResponseReader reader = TemplateResponseReaderBuilder.getInstance().setTemplateResource(uri).build();
        // use reader
        String response = "511,2";
        PublishedValues publishedValues = reader.read(response);
        // check
        assertThat(publishedValues.size(), Matchers.equalTo(0));
        assertThat(publishedValues.isValid(), Matchers.equalTo(true));
    }
}
