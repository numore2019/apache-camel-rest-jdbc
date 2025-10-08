package com.camel.demojdbc.routes;

import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.jacksonxml.JacksonXMLDataFormat;
import org.apache.camel.model.rest.RestBindingMode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;


@Component
public class JsonRoutes extends RouteBuilder {

    @Value("${server.host}")
    private String serverHost;

    @Value("${server.port}")
    private Integer serverPort;

    @Autowired
    private JacksonXMLDataFormat mapXmlDataFormat; // Para Map

    @Override
    public void configure() throws Exception {

        // Configuración REST
        restConfiguration()
                .component("jetty")
                .host(serverHost)
                .port(serverPort)
                .contextPath("/api/v1/camel")
                .enableCORS(true)
                .bindingMode(RestBindingMode.off);

        // Ruta REST Json
        rest("/products-json")
                .post()
                .consumes("application/json")
                .produces("application/json")
                .to("direct:processJson");

        // Ruta json a xml
        from("direct:processJson")
                .unmarshal().json() // JSON → Map
                .setBody(simple(
                        "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
                                + "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" "
                                + "xmlns:ord=\"http://example.com/order\">"
                                + "<soapenv:Header/>"
                                + "<soapenv:Body>"
                                + "<ord:GetOrderInfoRequest>"
                                + "<ord:OrderId>${body[OrderId]}</ord:OrderId>"
                                + "<ord:RequestDate>${body[RequestDate]}</ord:RequestDate>"
                                + "<ord:Requester>"
                                + "<ord:UserId>${body[Requester][UserId]}</ord:UserId>"
                                + "<ord:System>${body[Requester][System]}</ord:System>"
                                + "</ord:Requester>"
                                + "</ord:GetOrderInfoRequest>"
                                + "</soapenv:Body>"
                                + "</soapenv:Envelope>"
                )) // Map → XML SOAP
                .setHeader("Content-Type", constant("text/xml"))
                .to("https://user-soap.free.beeceptor.com/json?bridgeEndpoint=true")
                .unmarshal(mapXmlDataFormat)                   // XML → Map
                .log("=== Respuesta XML de Beeceptor convertida a Map: ${body}")
                .marshal().json()                              // Map → JSON
                .setHeader("Content-Type", constant("application/json"));

       }
}
