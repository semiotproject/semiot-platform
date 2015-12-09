FROM nginx:1.9

RUN apt-get update -y && apt-get install -y vim

ADD proxy.conf /etc/nginx/conf.d/default.conf
