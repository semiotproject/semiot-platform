FROM fedora

WORKDIR /

ENV NGINX_DEST /usr/share/nginx/html

RUN yum upgrade -y && yum clean all
RUN yum install -y nginx && yum clean all

ADD nginx.conf /etc/nginx/nginx.conf

ADD ./ $NGINX_DEST/

RUN chmod 777 -R $NGINX_DEST/*

EXPOSE 80

CMD ["/usr/sbin/nginx"]