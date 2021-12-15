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
test: test-clj ## Test
	hadolint deployments/production/Dockerfile
	shellcheck entrypoint.sh deployments/production/kubectl.sh
	(cd deployments/production && kustomize build) | kubeval --ignore-missing-schemas --strict
	npx prettier --check README.md
	npx prettier --check .github/workflows/*.yml deployments/*/*.yaml

.PHONY: test-clj
test-clj:
	cljstyle check
	cljstyle find | xargs -t clj-kondo --lint

.PHONY: upgrade
upgrade: ## Upgrade deps
	clojure -M:dev -m antq.core
