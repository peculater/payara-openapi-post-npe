#!/bin/bash
mvn package payara-micro:bundle
java -jar target/openapinpe-microbundle.jar

