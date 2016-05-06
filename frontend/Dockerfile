FROM nginx:1.9

ENV NGINX_HOME /etc/nginx

ADD proxy.conf $NGINX_HOME/conf.d/default.conf
ADD default.key $NGINX_HOME/ssl/server.key
ADD default.crt $NGINX_HOME/ssl/server.crt
