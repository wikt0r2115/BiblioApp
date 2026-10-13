#!/usr/bin/env bash
set -euo pipefail

api="${BIBLIOAPP_API:-http://127.0.0.1:8080}"
nonce="$(date +%s%N)"
isbn12="978${nonce: -9}"
sum=0
for ((i = 0; i < 12; i++)); do
  digit="${isbn12:i:1}"
  if ((i % 2 == 0)); then
    sum=$((sum + digit))
  else
    sum=$((sum + digit * 3))
  fi
done
isbn="${isbn12}$(((10 - sum % 10) % 10))"

post_json() {
  curl -fsS -H 'Content-Type: application/json' -d "$2" "$api$1"
}

author="$(post_json /author '{"firstName":"Demo","lastName":"Smoke"}' | jq -er '.id')"
category="$(post_json /category "$(jq -nc --arg name "Smoke $nonce" '{name:$name}')" | jq -er '.categoryId')"
member="$(post_json /member "$(jq -nc --arg email "smoke-$nonce@example.test" '{firstName:"Demo",lastName:"Reader",email:$email}')" | jq -er '.memberId')"
book="$(post_json /book "$(jq -nc --arg isbn "$isbn" --argjson author "$author" --argjson category "$category" '{isbn:$isbn,title:"Smoke book",publicationYear:2025,authorId:$author,categoryIds:[$category]}')" | jq -er '.id')"
request="$(jq -nc --argjson book "$book" --argjson member "$member" '{bookId:$book,memberId:$member}')"
loan="$(post_json /loans "$request" | jq -er '.loanId')"

test "$(curl -sS -o /dev/null -w '%{http_code}' -H 'Content-Type: application/json' -d "$request" "$api/loans")" = 409
curl -fsS "$api/book/$book" | jq -e '.available == false' >/dev/null
curl -fsS -X POST "$api/loans/$loan/return" | jq -e '.active == false and .returnedAt != null' >/dev/null
test "$(curl -sS -o /dev/null -w '%{http_code}' -X POST "$api/loans/$loan/return")" = 409
test "$(curl -sS -o /dev/null -w '%{http_code}' -X DELETE "$api/book/$book")" = 204
test "$(curl -sS -o /dev/null -w '%{http_code}' -X DELETE "$api/member/$member")" = 204
curl -fsS "$api/members/$member/loans/history" | jq -e --argjson loan "$loan" 'any(.[]; .loanId == $loan and .active == false)' >/dev/null
printf '%s\n' "$loan"
