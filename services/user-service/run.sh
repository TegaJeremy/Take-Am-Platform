#!/bin/bash
export $(cat ../../.env | grep -v '^#' | xargs)
mvn spring-boot:run
