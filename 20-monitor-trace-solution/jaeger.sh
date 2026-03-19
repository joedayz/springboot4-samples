#!/bin/bash

# Inicia Jaeger (all-in-one) con soporte OTLP.

set -e

docker run --rm --name jaeger \
  -e COLLECTOR_OTLP_ENABLED=true \
  -p 4317:4317 \
  -p 4318:4318 \
  -p 16686:16686 \
  -p 14268:14268 \
  jaegertracing/all-in-one:1.57

