package com.camel.demojdbc.routes;

import com.camel.demojdbc.models.Contacts;
import com.camel.demojdbc.models.Contact;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.jacksonxml.JacksonXMLDataFormat;
import org.apache.camel.model.rest.RestBindingMode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class XmlRoutes extends RouteBuilder {

    @Value("${server.host}")
    private String serverHost;

    @Value("${server.port}")
    private Integer serverPort;

    @Autowired
    private JacksonXMLDataFormat contactsXmlDataFormat;

    @Override
    public void configure() {

        // Configuración REST con Spring Boot properties
        restConfiguration()
                .component("jetty")
                .host(serverHost)
                .port(serverPort)
                .contextPath("/api/v1/camel")
                .enableCORS(true)
                .bindingMode(RestBindingMode.off);

        // Ruta REST XML
        rest("/users-xml")
                .post()
                .consumes("application/xml")
                .produces("application/xml")
                .to("direct:processXml");

        // ruta xml a json
        from("direct:processXml")
                .unmarshal(contactsXmlDataFormat)                     // XML recibido → objeto Contacts.
                .marshal().json()                             // Objeto Contacts → JSON para enviarlo al endpoint externo.
                .setHeader("Content-Type", constant("application/json")) // Asegurar que la petición HTTP tenga el header correcto indicando JSON.
                .to("https://postman-echo.com/post?bridgeEndpoint=true") // Enviar el JSON al endpoint de prueba Postman Echo.
                .log("=== Respuesta de Postman Echo: ${body}")             // Se imporime la respuesta que viene de Postman Echo
                .unmarshal().json(Map.class)                  // 6 Convertir la respuesta JSON en un Map de Java para poder acceder a sus valores.
                .process(exchange -> {                        // 7 Procesar la respuesta y convertirla nuevamente a objeto Contacts
                    Map<?, ?> resp = exchange.getIn().getBody(Map.class); // Obtener el body como Map
                    Contacts contacts = new Contacts();                   // Crear el objeto Contacts que será el resultado final
                    if (resp != null && resp.containsKey("json")) {       // Revisar si la respuesta tiene el nodo "json"
                        Map<?, ?> json = (Map<?, ?>) resp.get("json");    // Obtener el contenido del nodo "json"
                        List<?> contactslist = (json != null) ? (List<?>) json.get("contacts") : null; // Extraer la lista de contactos
                        if (contactslist != null) {                                // Si hay contactos en la lista
                            List<Contact> list = new java.util.ArrayList<>();
                            ObjectMapper mapper = new ObjectMapper();        // Crear ObjectMapper para convertir cada elemento
                            for (Object obj : contactslist)                          // Recorrer cada contacto
                                list.add(mapper.convertValue(obj, Contact.class)); // Convertirlo a Contact y agregarlo a la lista
                            contacts.setContacts(list);                     // Asignar la lista completa al objeto Contacts
                        }
                    }
                    exchange.getMessage().setBody(contacts);            // Poner el objeto Contacts en el body del mensaje Camel
                })
                .marshal(contactsXmlDataFormat)                                // 8 Convertir el objeto Contacts de nuevo a XML para la respuesta REST
                .log("=== XML final que se retorna: ${body}");         // 9 Imprimir en consola el XML final que se devolverá al cliente

    }
}
