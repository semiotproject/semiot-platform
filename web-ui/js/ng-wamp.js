(function(angular, autobahn, console){
    var services = angular.module('ngWAMP', []);
    
    services.factory('wamp', function($q) {
        var globalDeferred;
        
        function WampClient() {
            this.connections = {};
        }
        
        WampClient.prototype = {
            _newConnection: function(serverUrl) {
                var conn = new autobahn.Connection({
                    url: serverUrl,
                    realm: 'realm1'
                });
                return conn;
            },
            _open: function(connection) {
                var deferred = $q.defer();
                if(connection.isOpen) {
                    console.log('Connection to WAMP router [' 
                            + connection.transport.info.url + '] is already open');
                    deferred.resolve(connection.session);
                } else {
                    connection.onopen = function(session) {
                        console.log('Connected to WAMP router [' 
                                + connection.transport.info.url + "]");
                        deferred.resolve(session);
                    };
                    connection.onclose = function(reason, details) {
                        console.log('Connection to WAMP router [' 
                                + connection.transport.info.url 
                                + '] has been closed. Reason: ' + reason);
                    };
                    connection.open();
                }
                return deferred.promise;
            }
        };
        
        WampClient.prototype.subscribe = function(streamUri, callback) {
            var deferred = $q.defer();
            var uri = parseUri(streamUri);
            var serverUri = uri.protocol + '://' + uri.host;
            serverUri += (uri.port? ':' + uri.port : '') + uri.path;
            if(!this.connections.hasOwnProperty(serverUri)) {
                this.connections[serverUri] = this._newConnection(serverUri);
            }
            var connection = this.connections[serverUri];
            
            /**
             * The connect should not be called while the previous one 
             * has not finished yet.
             */
            var thisArg = this;
            if(!globalDeferred) {
                globalDeferred = $q.defer();
            }
            globalDeferred = $q.when(globalDeferred)
            .then(function() {
                return thisArg._open(connection); 
            })
            .then(function(session) {
                session.subscribe(uri.queryKey['topic'], callback)
                .then(function(subscription) {
                    console.log('Subscribed to [' + streamUri + '] stream');
                    deferred.resolve(subscription);
                }, function(error) {
                    console.log('Failed to subscribe to [' + streamUri
                            + ']. Error: ' + error);
                });
            }, function() {
                deferred.reject();
            });
            return deferred.promise;
        };
        
        return new WampClient();
    });
})(window.angular, window.autobahn, window.console);