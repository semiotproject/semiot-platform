log=$HBASE_HOME/logs/log
service ssh restart

while !curl -Is localhost:2122 :
do
	echo "Try to connect with ssh server" > $log
	sleep 1
done

echo "SSH server started!" > $log

trap 'echo STOPING HBASE >> $log; $HBASE_HOME/bin/stop-hbase.sh >> $log; 
	echo "HBASE successfully stoped!" >> $log; exit' HUP INT TERM EXIT
echo "Starting hbase" >> $log
$HBASE_HOME/bin/start-hbase.sh >> $log
echo "HBase started" >> $log
if [ ! -e /opt/hbase/logs/isCreatedTables ]
then
        echo "Create tables for OpenTSDB" >> $log
        /opt/create_table.sh >> $log
        echo "Tables created" >> $log
        touch /opt/hbase/logs/isCreatedTables
fi
while :
do
sleep 30
done
