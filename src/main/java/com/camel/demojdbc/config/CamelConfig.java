package com.camel.demojdbc.config;

import com.camel.demojdbc.models.Contacts;
import org.apache.camel.component.jacksonxml.JacksonXMLDataFormat;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import java.util.Map;

@Configuration
public class CamelConfig {

    @Bean
    public JacksonXMLDataFormat contactsXmlDataFormat() {
        return new JacksonXMLDataFormat(Contacts.class);
    }

    @Bean
    public JacksonXMLDataFormat mapXmlDataFormat() {
        return new JacksonXMLDataFormat(Map.class);
    }
}
