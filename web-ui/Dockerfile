# pull base image
FROM ubuntu

# install git (bower require it)
RUN apt-get install -y git

# install NGINX
RUN apt-get install -y software-properties-common && \
    add-apt-repository -y ppa:nginx/stable && \
    apt-get update && \
    apt-get install -y nginx && \
    rm -rf /var/lib/apt/lists/* && \
    chown -R www-data:www-data /var/lib/nginx

# add NGINX conf
ADD ./nginx.conf /etc/nginx/nginx.conf

# install node and npm
RUN apt-get update && \
    apt-get -qq update && \
    apt-get install -y nodejs npm

# debian installs `node` as `nodejs`
RUN update-alternatives --install /usr/bin/node node /usr/bin/nodejs 10

# build app
ADD . /webapp
RUN cd /webapp && npm i -g grunt-cli && npm i -g bower &&  npm run install-all && npm run build
RUN cp -rf /webapp/dist /var/www

# Define working directory.
WORKDIR /etc/nginx

# Define default command.
CMD ["nginx"]

# Expose ports.
EXPOSE 80
EXPOSE 443
