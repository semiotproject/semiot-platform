## Examples of commands

```
@prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#> .
@prefix ssn: <http://purl.oclc.org/NET/ssnx/ssn#> .
@prefix xsd: <http://www.w3.org/2001/XMLSchema#> .
@prefix dul: <http://www.loa-cnr.it/ontologies/DUL.owl#> .
@prefix dcterms: <http://purl.org/dc/terms/> .
@prefix proto: <http://w3id.org/semiot/ontologies/proto#> .
@prefix semiot: <http://w3id.org/semiot/ontologies/semiot#> .
@prefix sh: <http://www.w3.org/ns/shacl#> .
@prefix : <https://raw.githubusercontent.com/semiotproject/semiot-platform/release-1.0.7/device-proxy-service-drivers/mock-plain-lamp/src/main/resources/ru/semiot/drivers/mocks/plainlamp/prototype.ttl#> .

[ a semiot:Command, semiot:StartCommand ;
  semiot:forProcess <https://localhost/systems/.../processes/light> ;
  dul:associatedWith <https://localhost/systems/...> ;
  dul:hasParameter [ a semiot:MappingParameter ;
    semiot:forParameter :PlainLamp-Shine-Lumen;
    dul:hasParameterDataValue "890"^^xsd:integer ;
  ] ;
  dul:hasParameter [ a semiot:MappingParameter ;
    semiot:forParameter :PlainLamp-Shine-Color ;
    dul:hasParameterDataValue "4000"^^xsd:integer ;
  ] ;
] .
```