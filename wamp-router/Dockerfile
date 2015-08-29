FROM fedora

WORKDIR /root

RUN dnf install libffi-devel python-pip python-devel gcc openssl openssl-devel git -y && dnf clean all
RUN dnf upgrade -y && dnf clean all
RUN pip install --upgrade pip
RUN pip install --upgrade twisted
RUN pip install ez_setup six pyopenssl pycrypto wsaccel ujson
RUN pip install --upgrade distribute
RUN pip install crossbar[tls,msgpack,manhole,system]
#Remove pip cache
RUN rm -rf /tmp/pip_build_root/

RUN git clone https://github.com/pyca/pyopenssl.git
RUN cd pyopenssl && python setup.py install
RUN crossbar version
RUN mkdir crossbar

WORKDIR crossbar

RUN crossbar init
ADD config.json /root/crossbar/.crossbar/
RUN openssl version
RUN openssl genrsa -out .crossbar/server_key.pem 2048
RUN openssl req -new -key .crossbar/server_key.pem -out .crossbar/server_csr.pem -subj "/C=RU/ST=Saint-Petersburg/L=Saint-Petersburg/O=ITMO University/OU=Laboratory ISST/CN=localhost"
RUN openssl x509 -req -days 365 -in .crossbar/server_csr.pem -signkey .crossbar/server_key.pem -out .crossbar/server_cert.pem
RUN openssl dhparam -2 1024 -out .crossbar/dhparam.pem

EXPOSE 8080 8443

CMD crossbar start --cbdir /root/crossbar/.crossbar
