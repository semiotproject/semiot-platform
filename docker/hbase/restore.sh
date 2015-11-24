#!/bin/bash
if [ $# != 0 ] && [ -d /etc/hbase/backup/$1/ ]; then
	/bin/bash /opt/hbase/bin/hbase org.apache.hadoop.hbase.mapreduce.Import tsdb /etc/hbase/backup/$1/tsdb/
	/bin/bash /opt/hbase/bin/hbase org.apache.hadoop.hbase.mapreduce.Import tsdb-meta /etc/hbase/backup/$1/tsdb-meta/
	/bin/bash /opt/hbase/bin/hbase org.apache.hadoop.hbase.mapreduce.Import tsdb-tree /etc/hbase/backup/$1/tsdb-tree/
	/bin/bash /opt/hbase/bin/hbase org.apache.hadoop.hbase.mapreduce.Import tsdb-uid /etc/hbase/backup/$1/tsdb-uid/
else
	echo "Wrong path to backup!"
fi