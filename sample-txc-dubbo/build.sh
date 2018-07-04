mvn package
cp ../lib/txc-client-2.0.72.jar client/lib/
cd client/bin
sed 's/sample-txc-dubbo-0.2.1-SNAPSHOT.jar/sample-txc-dubbo-0.2.1-SNAPSHOT.jar:"$REPO"\/txc-client-2.0.72.jar/;s/-Dapp.repo="$REPO"/-Dapp.repo="$REPO"   -Djava.net.preferIPv4Stack=true /' ./order.sh >order_run.sh
sed 's/sample-txc-dubbo-0.2.1-SNAPSHOT.jar/sample-txc-dubbo-0.2.1-SNAPSHOT.jar:"$REPO"\/txc-client-2.0.72.jar/;s/-Dapp.repo="$REPO"/-Dapp.repo="$REPO"   -Djava.net.preferIPv4Stack=true /' ./stock.sh >stock_run.sh
sed 's/sample-txc-dubbo-0.2.1-SNAPSHOT.jar/sample-txc-dubbo-0.2.1-SNAPSHOT.jar:"$REPO"\/txc-client-2.0.72.jar/;s/-Dapp.repo="$REPO"/-Dapp.repo="$REPO"   -Djava.net.preferIPv4Stack=true /' ./client.sh >client_run.sh
chmod +x *.sh
