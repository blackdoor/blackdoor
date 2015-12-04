#!/usr/bin/env bash

if [ "$TRAVIS_BRANCH" = 'master' ] && [ "$TRAVIS_PULL_REQUEST" == 'false' ]; then
    openssl aes-256-cbc -K $encrypted_cfa46246aac3_key -iv $encrypted_cfa46246aac3_iv -in cd/signingkey.asc.enc -out cd/signingkey.asc -d
    gpg2 fast-import cd/signingkey.asc
fi