#!/bin/sh

SCRIPT_DIR=`dirname $0`

IMAGE_LIST=`grep -v -e "^\s*#" -e "^\s*$" ${SCRIPT_DIR}/image.list`

for image in ${IMAGE_LIST};
do
	cd ./${image}
	cp ../helidon-sample-util/target/helidon-sample-util.jar ./
	docker build . -t ${image}:latest
	cd ..
done
