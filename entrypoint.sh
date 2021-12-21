#!/bin/bash
set -eu

mkr wrap \
    -n mkr-gcloud-auto-retirement \
    -H "$MACKEREL_HOST_ID" \
    -a \
    -- clojure -M main.clj
