#!/usr/bin/env bash
set -euo pipefail

BASE_URL="${1:-${BASE_URL:-http://127.0.0.1:8080}}"
BASE_URL="${BASE_URL%/}"

log() { echo "[smoke] $*"; }
fail() {
  echo "[smoke] ERROR: $*" >&2
  exit 1
}

curl_text() {
  curl -fsS --max-time 10 "$@"
}

expect_api_ok() {
  local path="$1"
  local body
  body="$(curl_text "$BASE_URL$path")" || fail "GET $path failed"
  printf '%s' "$body" | grep -q '"code"[[:space:]]*:[[:space:]]*200' || fail "GET $path did not return code 200"
  log "GET $path ok"
}

expect_page_ok() {
  local path="$1"
  curl_text "$BASE_URL$path" | grep -qi '<html' || fail "GET $path did not return html"
  log "GET $path ok"
}

expect_api_ok "/api/health"
expect_api_ok "/api/plants?page=1&size=1"
expect_api_ok "/api/categories"
expect_page_ok "/"

if [ -n "${SMOKE_ACCOUNT:-}" ] && [ -n "${SMOKE_PASSWORD:-}" ]; then
  log "Checking authenticated /api/auth/me..."
  login_body="$(curl_text \
    -H 'Content-Type: application/json' \
    -d "{\"account\":\"$SMOKE_ACCOUNT\",\"password\":\"$SMOKE_PASSWORD\"}" \
    "$BASE_URL/api/auth/login")" || fail "POST /api/auth/login failed"
  token="$(printf '%s' "$login_body" | sed -n 's/.*"token"[[:space:]]*:[[:space:]]*"\([^"]*\)".*/\1/p')"
  [ -n "$token" ] || fail "login response did not contain token"
  me_body="$(curl_text -H "Authorization: Bearer $token" "$BASE_URL/api/auth/me")" || fail "GET /api/auth/me failed"
  printf '%s' "$me_body" | grep -q '"code"[[:space:]]*:[[:space:]]*200' || fail "GET /api/auth/me did not return code 200"
  log "GET /api/auth/me ok"
else
  log "Skipping auth smoke test; set SMOKE_ACCOUNT and SMOKE_PASSWORD to enable it"
fi

log "Smoke test passed for $BASE_URL"
