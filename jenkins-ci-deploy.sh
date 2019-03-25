#!/bin/bash
set -e

# Output of unit tests; path cannot contain spaces:
export TEST_LOG=/tmp/jenkins_rest_test_log_`date '+%s'`.log

# build container
make build-latest

# Unit tests: WormBase API and REST API
#make build-run-test | tee $TEST_LOG

# tag container
make docker-tag-latest

# push containers to AWS ECR
make docker-push-ecr-latest

# deploy container
make dockerrun-latest
cat Dockerrun.aws.json
git add Dockerrun.aws.json
git commit -m "use latest wormbase/rest container"  # only needed locally and subsequent build will discard this commit
make staging-deploy
