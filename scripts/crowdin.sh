#!/usr/bin/env bash

depends() {
    type $* >/dev/null || exit 1
}

depends git curl unzip

basedir=$(git rev-parse --show-toplevel)
apikey=$(tr -d ' \r\n' < "$basedir/scripts/crowdin.key")
apiurl="https://api.crowdin.com/api/project/slide-for-reddit"
location='app/src/main/res'
branch=${1:-application}

if [[ -z "$basedir" || -z "$apikey" ]]; then
    echo 'API key missing'
    exit 1
fi

if [[ -n "$(git status --porcelain $basedir/$location)" ]]; then
    echo 'Outstanding changes:'
    git status --short "$basedir/$location"
    exit 1
fi

response=$(curl -sS "$apiurl/export?key=$apikey&branch=$branch" | grep '<success')
echo $response

if [[ -n "$response" ]]; then
    tempfile=$(mktemp)
    curl -sSo "$tempfile" "$apiurl/download/all.zip?key=$apikey&branch=$branch"
    unzip -oqd "$basedir/$location" "$tempfile"
    rm "$tempfile"
    git --no-pager diff --stat --no-ext-diff "$basedir/$location"
fi
