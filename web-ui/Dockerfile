FROM kyma/docker-nginx

ADD ./nginx.conf /etc/nginx/nginx.conf

RUN apt-get update
RUN apt-get -qq update
RUN apt-get install -y nodejs npm

# debian installs `node` as `nodejs`
RUN update-alternatives --install /usr/bin/node node /usr/bin/nodejs 10

ADD . /webapp
RUN cd /webapp && npm i -g grunt-cli && npm i -g bower &&  npm run install-all && npm run build

RUN cp -rf /webapp/dist /var/www

CMD 'nginx'