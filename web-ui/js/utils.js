var myModule = angular.module('utils', []);
myModule.factory('utils', function($q) {

	var parser = N3.Parser();
	var instance = {
		parse: function(str) {
			var deferred = $q.defer();

			parser.parse(str, function(error, triple, prefixes) {
				if (triple) {
					deferred.resolve(triple);
				}
			});	

			return deferred.promise;
		},
	    sparqlToHumanType: function(type) {
			for (var key in CONFIG.SPARQL.types) {
				if (CONFIG.SPARQL.types[key] === type) {
					return key;
				}
			}
			return null;
		}
	};

	return instance;
});