#!/bin/sh
mkdir -p /etc/opentsdb/ && touch /etc/opentsdb/opentsdb.conf && \
echo "tsd.http.request.cors_domains=*\ntsd.core.meta.enable_realtime_ts=True\ntsd.core.auto_create_metrics=True" > /etc/opentsdb/opentsdb.conf
# Bad bad practice
#Wait until hbase creates tables
flag=$(curl -s hbase:60010/table.jsp?name=tsdb | grep "Region Server" )
while [ -z "$flag" ]; do
	sleep 5
	echo "Sleep until hbase is creating tables"
	flag=$(curl -s hbase:60010/table.jsp?name=tsdb | grep "Region Server" )	
done
echo "Tables created"
echo "Start OpenTSDB"
/opt/opentsdb/build/tsdb tsd --port=4242 --staticroot=/opt/opentsdb/build/staticroot --cachedir=/opt/tmp/ --zkquorum=hbase:2181 >> /etc/opentsdb/log
echo "OpenTSDB started"
