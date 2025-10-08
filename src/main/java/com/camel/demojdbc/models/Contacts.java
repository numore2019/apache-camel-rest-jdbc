package com.camel.demojdbc.models;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import lombok.Data;
import java.util.List;

@Data
public class Contacts {
    @JacksonXmlElementWrapper(useWrapping = false) // indica que no hay un contenedor extra
    @JacksonXmlProperty(localName = "contact")    // cada elemento XML se llama <contact>
    private List<Contact> contacts;
}
