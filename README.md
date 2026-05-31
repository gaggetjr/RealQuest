# Quest MVP

일일/주간 퀘스트를 등록하고 완료 체크를 하면 달성률과 EXP가 올라가는 Android MVP 앱입니다.

## 포함 기능

- 일일 퀘스트 추가
- 주간 퀘스트 추가
- 퀘스트 완료 체크
- 퀘스트 삭제
- 오늘 달성률 표시
- 이번 주 달성률 표시
- 메인 화면 전투력 표시
- 메인 화면 보스 입장
- 드래곤 보스전
  - 드래곤 HP 5000
  - 총 5회 자동 공격
  - 공격 간격 0.3초
  - 전투력만큼 피해
  - 빨간 현재 체력 / 노란 지연 체력 / 검은 손실 체력바
  - 승리/패배 결과 표시 후 메인 복귀
- 완료 순간 누적 EXP 저장
- 체크 해제해도 레벨/EXP 유지
- 같은 일일/주간 기간에는 같은 퀘스트 EXP 중복 지급 방지
- 퀘스트 및 설정 화면의 레벨 초기화
- 레벨 초기화 전 확인 창 표시
- 보상 EXP 직접 입력 지원
- 첫 실행 시 닉네임 설정
- 메인 화면에 `칭호 + 닉네임` 표시
- 보상 화면에서 해금한 칭호 장착
- 메인 닉네임/칭호 표시 확대
- 보상 화면에서 레벨/히든 보상 분리
- 히든 칭호
  - 노련한 모험가: 일주일간 매일 일일 퀘스트 100% 달성 시 `노련한` 칭호
  - 1렙 몬스터 한테 객사: 일주일간 매일 일일 퀘스트 0% 달성 시 `나태한` 칭호
- 퀘스트 및 설정 화면에서 닉네임 재설정
- 레벨별 필요 EXP 증가
  - 1~5레벨 구간: 100부터 25씩 증가
  - 5~10레벨 구간: 50씩 증가
  - 10레벨 이후: 100씩 증가
- Room DB 저장
- 날짜/주차가 바뀌면 완료 상태 자동 초기화
- 앱 시작 시 `REAL QUEST` 로고 스플래시 2초 표시
- 상단 탭 화면 전환
  - 메인
  - 업적
  - 보상
  - 퀘스트 및 설정

## 주요 파일

- `app/src/main/java/com/example/questmvp/MainActivity.kt`
  - Compose 화면 전체
- `app/src/main/java/com/example/questmvp/data/Quest.kt`
  - 퀘스트 데이터 구조
- `app/src/main/java/com/example/questmvp/data/QuestDao.kt`
  - Room DB 쿼리
- `app/src/main/java/com/example/questmvp/data/QuestDatabase.kt`
  - Room 데이터베이스
- `app/src/main/java/com/example/questmvp/data/QuestRepository.kt`
  - 퀘스트 추가, 완료, 삭제, 리셋 로직
- `app/src/main/java/com/example/questmvp/viewmodel/QuestViewModel.kt`
  - 달성률, EXP, 레벨 계산

## 추가 할 기능

- 연속 달성 streak
- 퀘스트 난이도
- 통계 화면
