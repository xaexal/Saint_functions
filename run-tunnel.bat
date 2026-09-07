@echo off
REM 이 컴퓨터(로컬 개발 PC) 전용 실행 스크립트.
REM 원격 DB에 SSH 터널을 경유해 접속하도록 강제로 tunnel 모드를 지정한다.
call gradlew.bat bootRun --args="--app.datasource.mode=tunnel"
