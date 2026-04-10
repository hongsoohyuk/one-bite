#!/bin/bash
# OCI ARM A1 인스턴스 품절 시 자동 재시도
# 사용법: ./retry-apply.sh

INTERVAL=600  # 재시도 간격 (초, 10분)
LOG_FILE="retry-apply.log"
attempt=0

echo "=== retry-apply 시작: $(date) ===" | tee -a "$LOG_FILE"

while true; do
  attempt=$((attempt + 1))
  echo "" | tee -a "$LOG_FILE"
  echo "[시도 #${attempt}] $(date '+%Y-%m-%d %H:%M:%S') terraform apply 시도..." | tee -a "$LOG_FILE"
  tmpfile=$(mktemp)
  terraform apply -auto-approve 2>&1 | tee "$tmpfile"
  result=$(cat "$tmpfile")
  rm -f "$tmpfile"

  if echo "$result" | grep -q "Out of host capacity"; then
    echo "[시도 #${attempt}] $(date '+%Y-%m-%d %H:%M:%S') ❌ 실패 — 품절 (Out of host capacity). ${INTERVAL}초($(( INTERVAL / 60 ))분) 후 재시도..." | tee -a "$LOG_FILE"
    sleep $INTERVAL
  elif echo "$result" | grep -q "Apply complete"; then
    echo "[시도 #${attempt}] $(date '+%Y-%m-%d %H:%M:%S') ✅ 성공!" | tee -a "$LOG_FILE"
    echo "$result" | grep -E "^(api_url|public_ip|ssh_command)" | tee -a "$LOG_FILE"
    echo "" | tee -a "$LOG_FILE"
    echo "=== 총 ${attempt}회 시도 후 성공 ===" | tee -a "$LOG_FILE"
    break
  else
    echo "[시도 #${attempt}] $(date '+%Y-%m-%d %H:%M:%S') ❌ 실패 — 예상치 못한 에러:" | tee -a "$LOG_FILE"
    echo "$result" | tee -a "$LOG_FILE"
    echo "" | tee -a "$LOG_FILE"
    echo "=== 총 ${attempt}회 시도 후 중단 ===" | tee -a "$LOG_FILE"
    break
  fi
done
