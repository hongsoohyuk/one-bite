#!/bin/bash
# OCI ARM A1 인스턴스 품절 시 자동 재시도
# 사용법: ./retry-apply.sh

INTERVAL=600  # 재시도 간격 (초, 10분)

while true; do
  echo "$(date): terraform apply 시도..."
  result=$(terraform apply -auto-approve 2>&1)

  if echo "$result" | grep -q "Out of host capacity"; then
    echo "$(date): 품절. ${INTERVAL}초 후 재시도..."
    sleep $INTERVAL
  elif echo "$result" | grep -q "Apply complete"; then
    echo ""
    echo "=== 성공! ==="
    echo "$result" | grep -E "^(api_url|public_ip|ssh_command)"
    break
  else
    echo "$(date): 예상치 못한 에러:"
    echo "$result"
    break
  fi
done
