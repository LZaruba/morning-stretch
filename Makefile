# Morning Stretch — build, deploy & run
#
# Použití:
#   make          # nápověda
#   make build    # sestaví JAR
#   make deploy   # zkopíruje JAR do ~/.minecraft/mods
#   make run      # spustí Minecraft s modem (dev režim přes Gradle)
#   make all      # build + deploy + run
#
# Vlastní cesta k Minecraftu:
#   make deploy MINECRAFT_DIR="/cesta/k/minecraft"

MOD_NAME     := morning-stretch
MOD_VERSION  := 1.0.0
JAR          := build/libs/$(MOD_NAME)-$(MOD_VERSION).jar
GRADLE       := ./gradlew
GRADLE_FLAGS := --no-daemon

UNAME_S := $(shell uname -s 2>/dev/null)
ifeq ($(UNAME_S),Darwin)
  MINECRAFT_DIR ?= $(HOME)/Library/Application Support/minecraft
else ifeq ($(UNAME_S),Linux)
  MINECRAFT_DIR ?= $(HOME)/.minecraft
else
  MINECRAFT_DIR ?= $(APPDATA)/.minecraft
endif

MODS_DIR := $(MINECRAFT_DIR)/mods

.PHONY: help build clean deploy run play all check-java

.DEFAULT_GOAL := help

help:
	@echo ""
	@echo "Morning Stretch — dostupné příkazy"
	@echo ""
	@echo "  make build    Sestaví mod ($(JAR))"
	@echo "  make deploy   Sestaví a zkopíruje JAR do: $(MODS_DIR)"
	@echo "  make run      Spustí testovací klienta s modem (./gradlew runClient)"
	@echo "  make play     Deploy + otevře Minecraft Launcher"
	@echo "  make all      build + deploy + run"
	@echo "  make clean    Smaže build složku"
	@echo ""
	@echo "Proměnné:"
	@echo "  MINECRAFT_DIR  Výchozí: $(MINECRAFT_DIR)"
	@echo ""
	@echo "Tip: Pro vývoj stačí 'make run' — mod se načte automaticky,"
	@echo "     deploy do .minecraft/mods není nutný."
	@echo ""

check-java:
	@java -version 2>&1 | head -1 || (echo "Chybí Java. Nainstaluj JDK 21+: https://adoptium.net/" && exit 1)

build: check-java
	$(GRADLE) $(GRADLE_FLAGS) build
	@test -f "$(JAR)" || (echo "Build selhal — JAR nenalezen: $(JAR)" && exit 1)
	@echo "✓ Sestaveno: $(JAR)"

clean:
	$(GRADLE) $(GRADLE_FLAGS) clean
	@echo "✓ Vyčištěno"

deploy: build
	@mkdir -p "$(MODS_DIR)"
	@rm -f "$(MODS_DIR)/$(MOD_NAME)-"*.jar
	@cp "$(JAR)" "$(MODS_DIR)/"
	@echo "✓ Nasazeno do: $(MODS_DIR)/$(MOD_NAME)-$(MOD_VERSION).jar"
	@echo ""
	@echo "  Nezapomeň mít nainstalované Fabric Loader + Fabric API pro 1.21.1"
	@echo "  a spusť profil 'Fabric 1.21.1' v Minecraft launcheru."

run: check-java
	@echo "→ Spouštím testovací klienta (mod se načte automaticky)..."
	$(GRADLE) $(GRADLE_FLAGS) runClient

play: deploy
	@echo "→ Otevírám Minecraft Launcher..."
ifeq ($(UNAME_S),Darwin)
	@open -a "Minecraft" 2>/dev/null || open "minecraft://"
else ifeq ($(UNAME_S),Linux)
	@minecraft-launcher 2>/dev/null || echo "Spusť Minecraft Launcher ručně a vyber profil Fabric 1.21.1"
else
	@cmd.exe /c start minecraft:// 2>/dev/null || echo "Spusť Minecraft Launcher ručně a vyber profil Fabric 1.21.1"
endif

all: deploy run
