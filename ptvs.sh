#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$SCRIPT_DIR"
STATE_DIR="$PROJECT_ROOT/.ptvs"
PID_DIR="$STATE_DIR/pids"
LOG_DIR="$STATE_DIR/logs"
CONFIG_FILE="$PROJECT_ROOT/ptvs-config.env"

CONTAINER_NAME="ptvs-postgres"
DEFAULT_VOLUME="ptvs-postgres-data"
POSTGRES_IMAGE="postgres:16.4"
DB_NAME="ptvs"
DB_USER="ptvs"
DB_PASSWORD="ptvs"

mkdir -p "$PID_DIR" "$LOG_DIR"

# Load configuration
if [[ -f "$CONFIG_FILE" ]]; then
  source "$CONFIG_FILE"
else
  DEBUG_ENABLED=false
  MAVEN_OPTS=""
  NODE_OPTIONS=""
fi

usage() {
  cat <<'EOF'
Usage:
  ./ptvs.sh --start [--components client,server]
  ./ptvs.sh --stop [--components client,server]
  ./ptvs.sh --startdb [--volume <volume-name>]
  ./ptvs.sh --stopdb
  ./ptvs.sh --exportdump <dump.dmp> [--volume <volume-name>]
  ./ptvs.sh --importdump <dump.dmp> [--volume <volume-name>]

Options:
  --start               Start server and client in the background.
  --stop                Stop server and client started by this script.
  --components LIST     Comma-separated component list (client,server). Default: both.
  --startdb             Start PostgreSQL container.
  --stopdb              Stop PostgreSQL container.
  --exportdump FILE     Export PostgreSQL dump in custom format (.dmp).
  --importdump FILE     Import PostgreSQL custom-format dump (.dmp).
  --volume NAME         Docker volume name for PostgreSQL data (default: ptvs-postgres-data).

Configuration:
  Edit ptvs-config.env to control:
  - DEBUG_ENABLED       Enable remote debugging on server (port 5005)
  - MAVEN_OPTS          Additional Maven options
  - NODE_OPTIONS        Additional Node options
EOF
}

require_command() {
  if ! command -v "$1" >/dev/null 2>&1; then
    echo "Missing required command: $1" >&2
    exit 1
  fi
}

is_pid_running() {
  local pid="$1"
  kill -0 "$pid" >/dev/null 2>&1
}

start_process() {
  local name="$1"
  local pid_file="$2"
  local log_file="$3"
  shift 3

  if [[ -f "$pid_file" ]]; then
    local existing_pid
    existing_pid="$(cat "$pid_file")"
    if [[ -n "$existing_pid" ]] && is_pid_running "$existing_pid"; then
      echo "$name is already running (PID $existing_pid). Stop it first."
      return
    fi
  fi

  nohup "$@" >"$log_file" 2>&1 &
  local new_pid=$!
  echo "$new_pid" >"$pid_file"
  echo "Started $name (PID $new_pid). Logs: $log_file"
}

stop_process() {
  local name="$1"
  local pid_file="$2"

  if [[ ! -f "$pid_file" ]]; then
    echo "$name is not running (no PID file)."
    return
  fi

  local pid
  pid="$(cat "$pid_file" 2>/dev/null || echo "")"

  if [[ -z "$pid" ]] || ! is_pid_running "$pid"; then
    rm -f "$pid_file"
    echo "$name is not running."
    return
  fi

  echo "Stopping $name (PID $pid)..."

  # Try graceful shutdown
  kill "$pid" 2>/dev/null || true
  sleep 1

  # If still alive, kill process group (most important for Vite)
  if is_pid_running "$pid"; then
    kill -TERM -- -"$pid" 2>/dev/null || true   # negative = process group
    sleep 1
  fi

  # Nuclear option
  if is_pid_running "$pid"; then
    kill -KILL "$pid" 2>/dev/null || true
    sleep 0.5
  fi

  # Extra cleanup: kill any Vite/Node processes in the client directory
  if [[ "$name" == "client" ]]; then
    pkill -9 -f "vite.*$PROJECT_ROOT/client" 2>/dev/null || true
    pkill -9 -f "node.*$PROJECT_ROOT/client" 2>/dev/null || true
  fi

  rm -f "$pid_file"
  echo "Stopped $name."
}

container_exists() {
  docker ps -a --format '{{.Names}}' | grep -Fx "$CONTAINER_NAME" >/dev/null 2>&1
}

container_running() {
  docker ps --format '{{.Names}}' | grep -Fx "$CONTAINER_NAME" >/dev/null 2>&1
}

start_db() {
  local volume_name="$1"
  require_command docker

  if container_running; then
    echo "PostgreSQL container is already running."
    return
  fi

  if container_exists; then
    docker start "$CONTAINER_NAME" >/dev/null
    echo "Started existing PostgreSQL container: $CONTAINER_NAME"
    return
  fi

  docker volume create "$volume_name" >/dev/null
  docker run -d \
    --name "$CONTAINER_NAME" \
    -e "POSTGRES_DB=$DB_NAME" \
    -e "POSTGRES_USER=$DB_USER" \
    -e "POSTGRES_PASSWORD=$DB_PASSWORD" \
    -p 5432:5432 \
    -v "$volume_name:/var/lib/postgresql/data" \
    "$POSTGRES_IMAGE" >/dev/null

  echo "Started PostgreSQL container: $CONTAINER_NAME (volume: $volume_name)"
}

stop_db() {
  require_command docker
  if container_running; then
    docker stop "$CONTAINER_NAME" >/dev/null
    echo "Stopped PostgreSQL container: $CONTAINER_NAME"
    return
  fi
  echo "PostgreSQL container is not running."
}

import_dump() {
  local dump_file="$1"
  local volume_name="$2"
  require_command docker

  if [[ ! -f "$dump_file" ]]; then
    echo "Dump file not found: $dump_file" >&2
    exit 1
  fi

  if [[ "$dump_file" != *.dmp ]]; then
    echo "Expected a .dmp file for --importdump: $dump_file" >&2
    exit 1
  fi

  start_db "$volume_name"
  docker exec -i "$CONTAINER_NAME" pg_restore \
    -U "$DB_USER" \
    -d "$DB_NAME" \
    --clean \
    --if-exists \
    --no-owner \
    --no-privileges <"$dump_file"
  echo "Imported dump into $DB_NAME from: $dump_file"
}

export_dump() {
  local dump_file="$1"
  local volume_name="$2"
  require_command docker

  if [[ "$dump_file" != *.dmp ]]; then
    echo "Expected a .dmp file for --exportdump: $dump_file" >&2
    exit 1
  fi

  local dump_dir
  dump_dir="$(dirname "$dump_file")"
  mkdir -p "$dump_dir"

  start_db "$volume_name"
  docker exec -i "$CONTAINER_NAME" pg_dump \
    -U "$DB_USER" \
    -d "$DB_NAME" \
    -Fc >"$dump_file"
  echo "Exported dump from $DB_NAME to: $dump_file"
}

start_apps() {
  require_command npm
  if [[ ! -x "$PROJECT_ROOT/mvnw" ]]; then
    echo "Missing executable Maven wrapper: $PROJECT_ROOT/mvnw" >&2
    exit 1
  fi

  if [[ ! -d "$PROJECT_ROOT/client/node_modules" ]]; then
    echo "Installing client dependencies..."
    (cd "$PROJECT_ROOT/client" && npm install)
  fi

  if [[ "$COMPONENTS" == *"client"* ]]; then
    local client_env="NODE_OPTIONS"
    [[ -n "$NODE_OPTIONS" ]] && client_env="$client_env='$NODE_OPTIONS'"
    start_process \
      "client" \
      "$PID_DIR/client.pid" \
      "$LOG_DIR/client.log" \
      bash -lc "cd '$PROJECT_ROOT/client' && npm run dev"
  fi

  if [[ "$COMPONENTS" == *"server"* ]]; then
    local mvn_cmd="./mvnw -pl server spring-boot:run"
    local debug_arg=""
    
    if [[ "$DEBUG_ENABLED" == "true" ]]; then
      debug_arg="-Dspring-boot.run.jvmArguments='-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:5005'"
      echo "Starting server with remote debugging enabled (port 5005)"
    fi
    
    start_process \
      "server" \
      "$PID_DIR/server.pid" \
      "$LOG_DIR/server.log" \
      bash -lc "cd '$PROJECT_ROOT' && MAVEN_OPTS='${MAVEN_OPTS}' ./mvnw -pl server spring-boot:run ${debug_arg}"
  fi
}

stop_apps() {
  if [[ "$COMPONENTS" == *"server"* ]]; then
    stop_process "server" "$PID_DIR/server.pid"
  fi
  if [[ "$COMPONENTS" == *"client"* ]]; then
    stop_process "client" "$PID_DIR/client.pid"
  fi
}

ACTION_START=false
ACTION_STOP=false
ACTION_START_DB=false
ACTION_STOP_DB=false
EXPORT_DUMP_FILE=""
DUMP_FILE=""
VOLUME_NAME="$DEFAULT_VOLUME"
COMPONENTS="client,server"

while [[ $# -gt 0 ]]; do
  case "$1" in
    --start)
      ACTION_START=true
      shift
      ;;
    --stop)
      ACTION_STOP=true
      shift
      ;;
    --components)
      [[ $# -lt 2 ]] && { echo "--components requires a value (e.g., client,server)." >&2; exit 1; }
      COMPONENTS="$2"
      shift 2
      ;;
    --startdb)
      ACTION_START_DB=true
      shift
      ;;
    --stopdb)
      ACTION_STOP_DB=true
      shift
      ;;
    --importdump)
      [[ $# -lt 2 ]] && { echo "--importdump requires a file path." >&2; exit 1; }
      DUMP_FILE="$2"
      shift 2
      ;;
    --exportdump)
      [[ $# -lt 2 ]] && { echo "--exportdump requires a file path." >&2; exit 1; }
      EXPORT_DUMP_FILE="$2"
      shift 2
      ;;
    --volume)
      [[ $# -lt 2 ]] && { echo "--volume requires a value." >&2; exit 1; }
      VOLUME_NAME="$2"
      shift 2
      ;;
    -h|--help)
      usage
      exit 0
      ;;
    *)
      echo "Unknown option: $1" >&2
      usage
      exit 1
      ;;
  esac
done

if ! $ACTION_START && ! $ACTION_STOP && ! $ACTION_START_DB && ! $ACTION_STOP_DB && [[ -z "$DUMP_FILE" ]] && [[ -z "$EXPORT_DUMP_FILE" ]]; then
  usage
  exit 1
fi

if $ACTION_START && $ACTION_STOP; then
  echo "Cannot use --start and --stop together." >&2
  exit 1
fi

if $ACTION_START_DB && $ACTION_STOP_DB; then
  echo "Cannot use --startdb and --stopdb together." >&2
  exit 1
fi

$ACTION_START && start_apps
$ACTION_STOP && stop_apps
$ACTION_START_DB && start_db "$VOLUME_NAME"
$ACTION_STOP_DB && stop_db
[[ -n "$EXPORT_DUMP_FILE" ]] && export_dump "$EXPORT_DUMP_FILE" "$VOLUME_NAME"
[[ -n "$DUMP_FILE" ]] && import_dump "$DUMP_FILE" "$VOLUME_NAME"
