<#include "common.sh.ftl">

if [ -z "$1" ]
then
  echo "This script is not meant to be executed directly. The watcher uses it to manage delegate processes."
  exit 0
fi

if [ -e proxy.config ]
then
  source proxy.config

  if [[ $PROXY_HOST != "" ]]
  then
    echo "Using $PROXY_SCHEME proxy $PROXY_HOST:$PROXY_PORT"
    export http_proxy=$PROXY_HOST:$PROXY_PORT
    export https_proxy=$PROXY_HOST:$PROXY_PORT
    PROXY_SYS_PROPS="-DproxyScheme=$PROXY_SCHEME -Dhttp.proxyHost=$PROXY_HOST -Dhttp.proxyPort=$PROXY_PORT -Dhttps.proxyHost=$PROXY_HOST -Dhttps.proxyPort=$PROXY_PORT"
  fi
  if [[ $NO_PROXY != "" ]]
  then
    echo "No proxy for domain suffixes $NO_PROXY"
    export no_proxy=$NO_PROXY
    SYSTEM_PROPERTY_NO_PROXY=`echo $NO_PROXY | sed "s/\,/|*/g"`
    PROXY_SYS_PROPS=$PROXY_SYS_PROPS" -Dhttp.nonProxyHosts=*$SYSTEM_PROPERTY_NO_PROXY"
    echo $PROXY_SYS_PROPS
  fi
fi

if [ ! -d $JRE_DIR  -o ! -d jre -o ! -e $JRE_BINARY ]
then
  echo "Downloading JRE packages..."
  JVM_TAR_FILENAME=$(basename "$JVM_URL")
  curl -#kLO $JVM_URL
  echo "Extracting JRE packages..."
  mkdir -p tmp
  mv $JVM_TAR_FILENAME tmp
  cd tmp
  tar xzf $JVM_TAR_FILENAME
  rm -rf ../$JRE_DIR
  mv $JRE_DIR_OLD ../$JRE_DIR
  cd ..
  rm -rf jre tmp
  ln -s $JRE_DIR jre
fi

echo "Checking Delegate latest version..."
DELEGATE_STORAGE_URL=${delegateStorageUrl}
REMOTE_DELEGATE_LATEST=$(curl -#k $DELEGATE_STORAGE_URL/${delegateCheckLocation})
REMOTE_DELEGATE_URL=$DELEGATE_STORAGE_URL/$(echo $REMOTE_DELEGATE_LATEST | cut -d " " -f2)
REMOTE_DELEGATE_VERSION=$(echo $REMOTE_DELEGATE_LATEST | cut -d " " -f1)
DEPLOY_MODE=${deployMode}

if [ ! -e delegate.jar ]
then
  echo "Downloading Delegate $REMOTE_DELEGATE_VERSION ..."
  curl -#k $REMOTE_DELEGATE_URL -o delegate.jar
else
  CURRENT_VERSION=$(unzip -c delegate.jar META-INF/MANIFEST.MF | grep Application-Version | cut -d "=" -f2 | tr -d " " | tr -d "\r" | tr -d "\n")
  if [[ $REMOTE_DELEGATE_VERSION != $CURRENT_VERSION ]]
  then
    echo "Downloading Delegate $REMOTE_DELEGATE_VERSION ..."
    mkdir -p backup.$CURRENT_VERSION
    cp delegate.jar backup.$CURRENT_VERSION
    curl -#k $REMOTE_DELEGATE_URL -o delegate.jar
  fi
fi

if [ ! -e config-delegate.yml ]; then
  echo "accountId: ${accountId}" > config-delegate.yml
  echo "accountSecret: ${accountSecret}" >> config-delegate.yml
fi
test "$(tail -c 1 config-delegate.yml)" && `echo "" >> config-delegate.yml`
if ! `grep managerUrl config-delegate.yml > /dev/null`; then
  echo "managerUrl: ${managerHostAndPort}/api/" >> config-delegate.yml
fi
if ! `grep watcherCheckLocation config-delegate.yml > /dev/null`
then
  echo "watcherCheckLocation: ${watcherStorageUrl}/${watcherCheckLocation}" >> config-delegate.yml
fi
if ! `grep heartbeatIntervalMs config-delegate.yml > /dev/null`; then
  echo "heartbeatIntervalMs: 60000" >> config-delegate.yml
fi
if ! `grep doUpgrade config-delegate.yml > /dev/null`; then
  echo "doUpgrade: true" >> config-delegate.yml
fi
if ! `grep localDiskPath config-delegate.yml > /dev/null`; then
  echo "localDiskPath: /tmp" >> config-delegate.yml
fi
if ! `grep maxCachedArtifacts config-delegate.yml > /dev/null`; then
  echo "maxCachedArtifacts: 2" >> config-delegate.yml
fi
if ! `grep proxy config-delegate.yml > /dev/null`; then
  echo "proxy: false" >> config-delegate.yml
fi
if ! `grep pollForTasks config-delegate.yml > /dev/null`; then
  if [ "$DEPLOY_MODE" == "ONPREM" ]; then
      echo "pollForTasks: true" >> config-delegate.yml
  else
      echo "pollForTasks: false" >> config-delegate.yml
  fi
fi

export HOSTNAME
export CAPSULE_CACHE_DIR="$DIR/.cache"
echo "Starting delegate - version $REMOTE_DELEGATE_VERSION"
$JRE_BINARY $PROXY_SYS_PROPS -Ddelegatesourcedir="$DIR" -Xmx4096m -XX:+HeapDumpOnOutOfMemoryError -XX:+PrintGCDetails -XX:+PrintGCDateStamps -Xloggc:mygclogfilename.gc -XX:+UseParallelGC -XX:MaxGCPauseMillis=500 -jar delegate.jar config-delegate.yml watched $1
sleep 3
if `pgrep -f "\-Ddelegatesourcedir=$DIR"> /dev/null`
then
  echo "Delegate started"
else
  echo "Failed to start Delegate."
  echo "$(tail -n 30 delegate.log)"
fi
