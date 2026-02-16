#!/bin/bash
# Load environment variables from parent .env
export $(cat ../../.env | grep -v '^#' | xargs)
mvn spring-boot:run
