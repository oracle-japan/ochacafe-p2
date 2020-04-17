#!/bin/sh

SCRIPT_DIR=`dirname $0`

kubectl apply -f ${SCRIPT_DIR}/yaml/jaeger-all-in-one.yaml
kubectl apply -f ${SCRIPT_DIR}/yaml/async-eshop-redis.yaml
kubectl apply -f ${SCRIPT_DIR}/yaml/monolith-eshop.yaml
kubectl apply -f ${SCRIPT_DIR}/yaml/api-eshop.yaml
kubectl apply -f ${SCRIPT_DIR}/yaml/api-eshop.yaml
