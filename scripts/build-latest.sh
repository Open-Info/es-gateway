#!/bin/bash

sbt stage
docker build -t "es-gateway:latest" .
