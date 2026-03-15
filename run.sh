#!/bin/bash
export $(cat .env | xargs)
./mvnw clean install
./mvnw spring-boot:run
