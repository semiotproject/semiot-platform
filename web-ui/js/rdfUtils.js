var myModule = angular.module('rdfUtils', []);
myModule.factory('rdfUtils', function($q) {

	var N3Utils = N3.Util;

	// helpers
	var toQName = function(uri) {
		if(!N3.Util.isQName(uri)) {
			Object.keys(CONFIG.SPARQL.prefixes).every(function(prefix) {
				var index = uri.indexOf(CONFIG.SPARQL.prefixes[prefix]);
				if(index > -1) {
					uri = prefix + ":" + uri.substring(CONFIG.SPARQL.prefixes[prefix].length);
					return false;
				}
				return true;
			}, this); 
		}
		return uri;
	};
	var expandQName = function(qname) {
		if(qname && N3Utils.isQName(qname)) {
			return N3Utils.expandQName(qname, CONFIG.SPARQL.prefixes);
		}
		return qname;
	};

	// Resource Class
	// TODO: make a separate service class
	var Resource = function(uri, type) {
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
				var result = -1;
				type.some(function(element, index) {
					var expandedType = expandQName(element);
					result = index;
					return this['http://www.w3.org/1999/02/22-rdf-syntax-ns#type'].indexOf(expandedType) > -1;
				}, this);
				return result;
			} else {
				var expandedType = expandQName(type);
				return this['http://www.w3.org/1999/02/22-rdf-syntax-ns#type'].indexOf(expandedType) > -1;
			}
		}
	};

	var instance = {
		parseTTL: function(str) { // string to triples
			var parser = N3.Parser();
			var defer = $q.defer();
			var triples = [];
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
				var resource = new Resource();
				triples.forEach(function(triple) {
					var p = toQName(triple.predicate);
					if(p === 'http://www.w3.org/1999/02/22-rdf-syntax-ns#type' && !resource.uri) { // FIXME: get root URI
						resource.uri = triple.subject;
					}
					if(!resource.hasOwnProperty(p)) {
						resource[p] = [];
					}
					resource[p].push(triple.object);
				});
				return resource;
			} else {
				return null;
			}
		}
	};

	return instance;
});