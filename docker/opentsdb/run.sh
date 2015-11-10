#!/bin/sh
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
/opt/opentsdb/build/tsdb tsd --port=4242 --staticroot=/opt/opentsdb/build/staticroot --cachedir=/opt/tmp/ --zkquorum=hbase:2181