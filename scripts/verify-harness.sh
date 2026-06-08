#!/usr/bin/env bash
# 하네스 훅 자가검증 (자기완결형 — 템플릿 fixtures 불필요).
set -u
ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
H="$ROOT/.claude/hooks/harness.mjs"
pass=0; fail=0
j(){ printf '{"tool_name":"Write","tool_input":{"file_path":"%s"}}' "$1"; }
guard_exit(){ j "$1" | node "$H" guard >/dev/null 2>&1; echo $?; }
SELF="$ROOT/src/main/java/__harness_selftest__"
trap 'rm -rf "$SELF"' EXIT
mkdir -p "$SELF/domain"
blocks=0
while IFS= read -r f; do
  case "$f" in *"__harness_selftest__"*) continue;; esac
  [ "$(guard_exit "$f")" -eq 2 ] && blocks=$((blocks+1))
done < <(find "$ROOT/src/main/java" -name '*.java')
if [ "$blocks" -eq 0 ]; then echo "  ✔ 실제 소스 가드 0 차단"; pass=$((pass+1)); else echo "  ✗ 실제 소스 차단 ${blocks}건"; fail=$((fail+1)); fi
cat > "$SELF/domain/SelfTestBad.java" <<'JAVA'
package __harness_selftest__.domain;
public class SelfTestBad { private String name; public void setName(String name){ this.name=name; } }
JAVA
if [ "$(guard_exit "$SELF/domain/SelfTestBad.java")" -eq 2 ]; then echo "  ✔ 도메인 공개 setter 위반 차단"; pass=$((pass+1)); else echo "  ✗ 합성 위반 미차단"; fail=$((fail+1)); fi
cat > "$SELF/domain/SelfTestOk.java" <<'JAVA'
package __harness_selftest__.domain;
public final class SelfTestOk { private final String name; public SelfTestOk(String name){ this.name=name; } public String name(){ return name; } }
JAVA
if [ "$(guard_exit "$SELF/domain/SelfTestOk.java")" -eq 0 ]; then echo "  ✔ 정상 도메인 파일 통과"; pass=$((pass+1)); else echo "  ✗ 정상 파일 오차단"; fail=$((fail+1)); fi
echo "── pass=$pass fail=$fail"
if [ "$fail" -eq 0 ]; then echo "✅ 하네스 훅 검증 완료"; exit 0; else echo "❌ 실패 있음"; exit 1; fi
