#!/bin/sh

SCRIPT_DIR=`dirname $0`

IMAGE_LIST=`grep -v -e "^\s*#" -e "^\s*$" ${SCRIPT_DIR}/image.list`

cd ./helidon-sample-util
mvn clean install
mvn clean package
mvn package
mvn install
cd ..

for image in ${IMAGE_LIST};
do
	cd ./${image}
	mvn clean install
	mvn clean package
	mvn package
	mvn install
	cd ..
done
