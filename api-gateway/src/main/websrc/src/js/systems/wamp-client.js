
"use strict";

import autobahn from 'autobahn';

let Logger = function() {
    this.$el = $("#message-log ul");
};
Logger.prototype = {
    constructor: Logger,
    append: function(message) {
        let time = new Date();
        let text =  time.getHours() + ":" + time.getMinutes() + ":" + time.getSeconds() + " " + message;
        let template = $(".message.template").clone(true).removeClass('template');
        template.find('p').text(text);
        this.$el.prepend(template);
    }
};

let logger = new Logger();

let connection;
$(document).on("ready", function() {
    $("#wamp-subscribe").on('click', function() {
        let url = $('.wamp-url input').val();
        let topic = $('.wamp-topic input').val();
        if(url && topic) {
            connection = new autobahn.Connection({
                url: url,
                realm: 'realm1'
            });
            logger.append("opening connection, url - " + url + ", topic - " + topic);
            $('.on-listen').show().siblings('.on-config').hide();
            $("#summary .url").text(url);
            $("#summary .topic").text(topic);
            connection.onopen = function (session) {
                logger.append("connection established");
                session.subscribe(topic, function(args) {
                    logger.append("message received: " + args[0]);
                });
                $("#wamp-send").on('click', function() {
                    let topic = $("#wamp-send-topic").val();
                    let text = $("#wamp-send-text").val();
                    session.publish(topic, [text]);
                    logger.append("publishing message " + text + " on topic " + topic);
                });
            };
            connection.onclose = function (reason, details) {
                // logger.append("connection closed: reason: " + reason + ", " + "details: " + details.message);
            };
            connection.open();
        }
    });
});