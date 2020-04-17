#!/bin/sh

SCRIPT_DIR=`dirname $0`
LOCAL_PORT=8081
PRODUCT_JSON=${SCRIPT_DIR}/data/product.json
USER_COUNT=100

### Setup product data to redis
echo "*** Setup product data for [mall] ***"

kubectl port-forward svc/async-eshop-mall ${LOCAL_PORT}:8080 > /dev/null &
PID=$!
sleep 3

curl --silent -X POST -H "Content-type: application/json" -d @${PRODUCT_JSON} http://localhost:${LOCAL_PORT}/product/update > /dev/null

kill -9 ${PID}

echo "...done."

### Setup cart data to redis
kubectl port-forward svc/async-eshop-cart ${LOCAL_PORT}:8080 > /dev/null  &
PID=$!
sleep 3

echo "*** Setup cart data for [cart] ***"
echo "*** Create ${USER_COUNT} users' carts ***"
for user_no in `seq ${USER_COUNT}`
do
  user_name="user${user_no}@oracle.com"
  echo -n "${user_name} "
  curl --silent -X POST -H "Content-type: application/json" http://localhost:${LOCAL_PORT}/cart/${user_name} > /dev/null
done

kill -9 ${PID}

echo "...done."
