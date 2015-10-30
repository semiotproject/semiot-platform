# pull base image
FROM ubuntu

# install NGINX
RUN apt-get install -y software-properties-common && \
    add-apt-repository -y ppa:nginx/stable && \
    apt-get update && \
    apt-get install -y nginx && \
    rm -rf /var/lib/apt/lists/* && \
    chown -R www-data:www-data /var/lib/nginx && \
    useradd nginx

# add NGINX conf
ADD ./nginx.conf /etc/nginx/nginx.conf

# add app
ADD ./dist/* /var/www/html/

# Define working directory.
WORKDIR /etc/nginx

# Define default command.
CMD ["nginx"]

# Expose ports.
EXPOSE 80
EXPOSE 443
