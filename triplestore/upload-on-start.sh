#!/bin/bash

sleep 2;

if [ -e "$TRIPLESTORE_UPLOAD_DIR/firststart.lock" ]
  then
    echo "[upload-on-start] Files already uploaded earlier!"
  else
    java -cp blazegraph.jar com.bigdata.rdf.store.DataLoader config.properties $TRIPLESTORE_UPLOAD_DIR
    echo "[upload-on-start] Succesfully uploaded files from $TRIPLESTORE_UPLOAD_DIR!"
    touch $TRIPLESTORE_UPLOAD_DIR/firststart.lock
fi

exit 0;
