#!/bin/bash
timestamp=$(date +%s)
/bin/bash /opt/hbase/bin/hbase org.apache.hadoop.hbase.mapreduce.Export tsdb /etc/hbase/backup/$timestamp/tsdb/
/bin/bash /opt/hbase/bin/hbase org.apache.hadoop.hbase.mapreduce.Export tsdb-meta /etc/hbase/backup/$timestamp/tsdb-meta/
/bin/bash /opt/hbase/bin/hbase org.apache.hadoop.hbase.mapreduce.Export tsdb-tree /etc/hbase/backup/$timestamp/tsdb-tree/
/bin/bash /opt/hbase/bin/hbase org.apache.hadoop.hbase.mapreduce.Export tsdb-uid /etc/hbase/backup/$timestamp/tsdb-uid/
echo "Backup created! Timestamp is $timestamp"