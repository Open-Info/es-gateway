#!/bin/bash

source ./scripts/build-latest.sh

# change this to your own API key
ES_API_KEY="secret"
IMAGE_ID=$(docker images -q es-gateway:latest)

docker run --rm -p 80:80 -e "ES_API_KEY=$ES_API_KEY" $IMAGE_ID
