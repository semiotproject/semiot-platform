"use strict";

import N3 from "n3";

export default function($q, CONFIG) {

    let N3Utils = N3.Util;

    // helpers
    let toPrefixedName = function(uri) {
        if(!N3.Util.isPrefixedName(uri)) {
            Object.keys(CONFIG.SPARQL.prefixes).every(function(prefix) {
                let index = uri.indexOf(CONFIG.SPARQL.prefixes[prefix]);
                if(index > -1) {
                    uri = prefix + ":" + uri.substring(CONFIG.SPARQL.prefixes[prefix].length);
                    return false;
                }
                return true;
            }, this);
        }
        return uri;
    };
    let expandPrefixedName = function(qname) {
        if(qname && N3Utils.isPrefixedName(qname)) {
            return N3Utils.expandPrefixedName(qname, CONFIG.SPARQL.prefixes);
        }
        return qname;
    };

    // Resource Class
    // TODO: make a separate service class
    let Resource = function(uri, type) {
        if (uri) {
            this.uri = uri;
        }
        if (type) {
            this['http://www.w3.org/1999/02/22-rdf-syntax-ns#type'] = [type];
        }
    };
    Resource.prototype = {
        constructor: Resource,
        get: function(property) {
            if(this[property]) {
                if(N3Utils.isLiteral(this[property][0])) {
                    return N3Utils.getLiteralValue(this[property][0]);
                }
                return this[property][0];
            }
            return null;
        },
        is: function(type) {
            if(Array.isArray(type)) {
                let result = -1;
                type.some(function(element, index) {
                    let expandedType = expandPrefixedName(element);
                    result = index;
                    return this['http://www.w3.org/1999/02/22-rdf-syntax-ns#type'].indexOf(expandedType) > -1;
                }, this);
                return result;
            }
            let expandedType = expandPrefixedName(type);
            return this['http://www.w3.org/1999/02/22-rdf-syntax-ns#type'].indexOf(expandedType) > -1;
        }
    };

    let instance = {
        parseTTL: function(str) { // string to triples
            let parser = N3.Parser();
            let defer = $q.defer();
            let triples = [];
            parser.parse(str, function(error, triple, prefixes) {
                if (triple) {
                    triples.push(triple);
                } else {
                    defer.resolve(triples);
                }
            });
            return defer.promise;
        },
        parseTriples: function(triples) { // triples to resource
            if(triples.length) {
                let resource = new Resource();
                triples.forEach(function(triple) {
                    let p = toPrefixedName(triple.predicate);
                    if(p === 'http://www.w3.org/1999/02/22-rdf-syntax-ns#type' && !resource.uri) { // FIXME: get root URI
                        resource.uri = triple.subject;
                    }
                    if(!resource.hasOwnProperty(p)) {
                        resource[p] = [];
                    }
                    resource[p].push(triple.object);
                });
                return resource;
            }
            return null;
        }
    };

    return instance;
}