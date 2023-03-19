#!/bin/bash

set -e

sbt stage
docker build -t "es-gateway:latest" .
