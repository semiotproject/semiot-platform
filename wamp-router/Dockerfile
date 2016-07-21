FROM crossbario/crossbar

RUN pip install --egg mysql-connector-python-rf

ADD config.json ./.crossbar/

ADD authenticator.py ./
