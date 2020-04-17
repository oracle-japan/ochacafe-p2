#!/bin/sh

SCRIPT_DIR=`dirname $0`
TAG_PREFIX="<region-key>.ocir.io/<tenancy-namespace>/<repo-name>"
IMAGE_LIST=`grep -v -e "^\s*#" -e "^\s*$" ${SCRIPT_DIR}/image.list`

for image in ${IMAGE_LIST};
do
  TARGET="${image}:latest"
  TAG_NAME=${TAG_PREFIX}/${image}:latest
  echo ${TAG_NAME}
  docker rmi --force ${TAG_NAME}
  docker tag ${TARGET} ${TAG_NAME}
  docker push ${TAG_NAME}
done
