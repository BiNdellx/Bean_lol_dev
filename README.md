# LoLDiscordBot

Java + JDA 기반 League of Legends Discord 서버용 봇의 데모 프로젝트입니다.

## Demo v0.1.0

현재 구현 범위는 의도적으로 작습니다.

1. Discord에서 `/line` 명령어 실행
2. 드롭다운 메뉴에서 라인 선택
   - 탑
   - 정글
   - 미드
   - 원딜
   - 서포터
3. 선택한 라인을 Discord 메시지로 출력

아직 역할 지급, DB 저장, 공지 확인, 채널 권한 변경은 구현하지 않았습니다.

## Requirements

- Linux: Ubuntu 26.04 목표
- Java 21
- `curl`
- `unzip`
- Discord Bot Token

프로젝트의 `run.sh`가 Gradle 9.7.1을 프로젝트 내부 `.gradle-dist/`에 자동 설치하므로 시스템에 Gradle을 별도로 설치할 필요가 없습니다.

## Discord Bot 준비

Discord Developer Portal에서 Application과 Bot을 생성하고 서버에 초대합니다.

이 데모는 메시지 내용을 읽지 않기 때문에 Message Content Intent를 요구하지 않습니다.

서버 초대 시 최소한 다음 scope를 사용합니다.

- `bot`
- `applications.commands`

## 실행

```bash
cp .env.example .env
nano .env
```

`.env`에 토큰을 입력합니다.

```env
BOT_TOKEN=your_real_bot_token
DISCORD_GUILD_ID=your_test_server_id
```

테스트 단계에서는 `DISCORD_GUILD_ID`를 넣는 것을 권장합니다. Guild command는 변경 사항을 빠르게 확인할 수 있습니다.

실행 권한을 부여합니다.

```bash
chmod +x run.sh build.sh scripts/gradle-bootstrap.sh
```

봇 실행:

```bash
./run.sh
```

`run.sh`는 다음 작업을 자동으로 수행합니다.

1. `.env` 로드
2. Gradle 9.7.1 존재 여부 확인
3. 필요한 경우 Gradle 다운로드
4. 프로젝트 빌드
5. 봇 실행

## Build only

```bash
./build.sh
```

빌드 결과는 다음 경로에 생성됩니다.

```text
build/install/LoLDiscordBot/
```

## Project structure

```text
LoLDiscordBot/
├── .env.example
├── .gitignore
├── build.gradle
├── settings.gradle
├── build.sh
├── run.sh
├── scripts/
│   └── gradle-bootstrap.sh
└── src/main/java/kr/loldiscordbot/
    ├── Main.java
    ├── bot/
    │   ├── BotListener.java
    │   └── Lane.java
    └── config/
        └── BotConfig.java
```

## Git

`.env`는 `.gitignore`에 포함되어 있으므로 실제 Bot Token은 저장소에 올라가지 않습니다.

```bash
git init
git add .
git commit -m "Initial Discord lane selection demo"
```

## Planned next step

다음 버전에서는 다음 흐름으로 확장할 수 있습니다.

```text
신규 유저 입장
  -> 주 라인 선택
  -> 부 라인 선택
  -> 공지 확인
  -> MEMBER 및 라인 역할 지급
  -> 일반 채널 공개
```
