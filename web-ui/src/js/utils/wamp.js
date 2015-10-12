"use strict";

import autobahn from 'autobahn';

export default function() {

    const instance = {
        subscribe: function(url, listeners) {
            let connection = new autobahn.Connection({
                url: url,
                realm: 'realm1'
            });
            connection.onopen = function(session) {
                listeners.forEach(function(listener) {
                    session.subscribe(listener.topic, listener.callback);
                });
            };
            connection.open();
            return connection;
        }
    };

    return instance;
}