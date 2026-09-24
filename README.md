# Bean_lol_dev

## Requirements

- **Java 21 JDK**
- **Git**
- **Discord Bot Token**
- **Discord Server ID (Guild ID)**
- Final execution environment: **Ubuntu 26.04**

Ubuntu ARM64 환경에서는 Java 21 JDK가 다음 경로에 설치되어 있어야 합니다.

```text
/usr/lib/jvm/java-21-openjdk-arm64
```

필수 패키지 설치:

```bash
sudo apt update
sudo apt install -y git openjdk-21-jdk
```

설치 확인:

```bash
java -version
javac -version
```

`javac` 버전은 **21**이어야 합니다.

## Installation

### 1. Repository Clone

```bash
git clone https://github.com/BiNdellx/Bean_lol_dev.git
cd Bean_lol_dev
```

### 2. Environment Configuration

프로젝트 루트에 `.env` 파일을 생성합니다.

```bash
nano .env
```

다음 값을 설정합니다.

```env
BOT_TOKEN=YOUR_DISCORD_BOT_TOKEN
DISCORD_GUILD_ID=YOUR_DISCORD_GUILD_ID
```

> `.env`에는 Discord Bot Token이 포함되므로 Git에 업로드하지 마세요.

### 3. Run

실행 권한을 부여합니다.

```bash
chmod +x run.sh
```

봇을 실행합니다.

```bash
./run.sh
```

`run.sh`는 Ubuntu ARM64 환경에서 Java 21 JDK를 다음 경로로 지정하여 실행합니다.

```text
/usr/lib/jvm/java-21-openjdk-arm64
```

GitHub의 최신 업데이트를 서버에 적용할 때는 다음 명령을 사용합니다.

```bash
git pull origin main
./run.sh
```

## License

이 프로젝트는 **개인 학습 및 비상업적 용도**로만 사용할 수 있습니다.

- 개인 학습 및 연구 목적의 사용을 허용합니다.
- 상업적 이용, 판매, 유료 서비스 제공 등 영리 목적의 사용을 허용하지 않습니다.
- 프로젝트 또는 소스 코드를 상업적 제품이나 서비스에 포함하여 사용할 수 없습니다.

Copyright © BiNdellx. All rights reserved.
