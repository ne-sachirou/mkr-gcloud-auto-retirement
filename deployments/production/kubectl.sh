#!/bin/bash -eux

set -eux

curl -s "https://raw.githubusercontent.com/kubernetes-sigs/kustomize/master/hack/install_kustomize.sh" | bash -ex
mv kustomize /usr/local/bin

cd deployments/production || exit 1

mkdir -p secret
set +x
echo -n "$PROJECT_ID" > secret/GCLOUD_PROJECT_ID
echo -n "$MACKEREL_APIKEY" > secret/MACKEREL_APIKEY
echo -n "$MACKEREL_HOST_ID" > secret/MACKEREL_HOST_ID
echo -n "$MACKEREL_ROLE" > secret/MACKEREL_ROLE
echo -n "$MACKEREL_SERVICE" > secret/MACKEREL_SERVICE
echo -n "$GCLOUD_SERVICE_ACCOUNT_KEY_JSON" > secret/gcloud_service_account_key.json
set -x

kustomize edit set image "asia.gcr.io/PROJECT_ID/mkr-gcloud-auto-retirement=asia.gcr.io/${PROJECT_ID}/mkr-gcloud-auto-retirement:${SHORT_SHA}"
kustomize build | \
/builder/kubectl.bash apply \
  -n mkr-gcloud-auto-retirement \
  -l app=mkr-gcloud-auto-retirement \
  --prune \
  -f -
