mvn package
cp ../lib/txc-client-2.0.72.jar txc-client-console/client/lib/
cd txc-client-console/client/bin
sed 's/txc-client-console-1.0-SNAPSHOT.jar/txc-client-console-1.0-SNAPSHOT.jar:"$REPO"\/txc-client-2.0.72.jar/' ./start.sh >run.sh
chmod +x run.sh
cd ../../../

cp ../lib/txc-client-2.0.72.jar txc-client-mq/client/lib/
cd txc-client-mq/client/bin
sed 's/txc-client-mq-1.0-SNAPSHOT.jar/txc-client-mq-1.0-SNAPSHOT.jar:"$REPO"\/txc-client-2.0.72.jar/' ./start.sh >run.sh
chmod +x run.sh
cd ../../../

cd txc-level-service/target
mkdir -p WEB-INF/lib
cp ../../../lib/txc-client-2.0.72.jar ./WEB-INF/lib
jar -uvf txc-level-service.war ./WEB-INF/lib/txc-client-2.0.72.jar
cd ../../

cd txc-money-service/target
mkdir -p WEB-INF/lib
cp ../../../lib/txc-client-2.0.72.jar ./WEB-INF/lib
jar -uvf txc-money-service.war ./WEB-INF/lib/txc-client-2.0.72.jar
cd ../../

cd txc-client-web/target
mkdir -p WEB-INF/lib
cp ../../../lib/txc-client-2.0.72.jar ./WEB-INF/lib
jar -uvf txc-client-web.war ./WEB-INF/lib/txc-client-2.0.72.jar
cd ../../