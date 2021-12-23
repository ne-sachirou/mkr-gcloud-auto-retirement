.PHONY: help
help:
	@awk -F':.*##' '/^[-_a-zA-Z0-9]+:.*##/{printf"%-12s\t%s\n",$$1,$$2}' $(MAKEFILE_LIST) | sort

.PHONY: format
format: ## Format files
	cljstyle fix
	npx prettier --write README.md
	npx prettier --write .github/workflows/*.yml deployments/*/*.yaml

.PHONY: run-local
run-local: ## Run the main script in the local computer
	./entrypoint.sh

.PHONY: run-remote
run-remote: ## Run the main script in the remote cluster
	kubectl create job -n mkr-gcloud-auto-retirement --from cronjob.batch/mkr-gcloud-auto-retirement "mkr-gcloud-auto-retirement-$(shell date +%s)"
	stern mkr-gcloud-auto-retirement --tail=4

.PHONY: test
test: test-clj test-k8s ## Test
	shellcheck entrypoint.sh
	npx prettier --check README.md
	npx prettier --check .github/workflows/*.yml
	yamllint .github/workflows/*.yml

.PHONY: test-clj
test-clj:
	cljstyle check
	cljstyle find | xargs -t clj-kondo --lint || true
	clojure -M:test

.PHONY: test-k8s
test-k8s: test-k8s-production
	npx prettier --check deployments/base/*.yaml
	yamllint deployments/base/*.yaml

.PHONY: test-k8s-production
test-k8s-production:
	npx prettier --check deployments/production/*.yaml
	yamllint deployments/production/*.yaml
	shellcheck deployments/production/kubectl.sh
	(cd deployments/production && kustomize build) | kubeval --ignore-missing-schemas --strict
	if which hadolint ; then hadolint deployments/production/Dockerfile ; fi
	docker build -t ne-sachirou/mkr-gcloud-auto-retirement:production -f deployments/production/Dockerfile --force-rm --pull .
	container-structure-test test --image ne-sachirou/mkr-gcloud-auto-retirement:production --config deployments/production/container-structure-test.yml
	#docker scan ne-sachirou/mkr-gcloud-auto-retirement:production || true

.PHONY: upgrade
upgrade: ## Upgrade deps
	clojure -M:dev -m antq.core || true
