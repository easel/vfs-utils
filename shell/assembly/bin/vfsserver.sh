#!/bin/sh

# Licensed to the Apache Software Foundation (ASF) under one
# or more contributor license agreements.  See the NOTICE file
# distributed with this work for additional information
# regarding copyright ownership.  The ASF licenses this file
# to you under the Apache License, Version 2.0 (the
# "License"); you may not use this file except in compliance
# with the License.  You may obtain a copy of the License at
#
#  http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing,
# software distributed under the License is distributed on an
# "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
# KIND, either express or implied.  See the License for the
# specific language governing permissions and limitations
# under the License.

#
# OS specific support.  $var _must_ be set to either true or false.
#
cygwin=false;
darwin=false;
case "`uname`" in
  CYGWIN*) cygwin=true ;;
  Darwin*) darwin=true
           if [ -z "$JAVA_HOME" ] ; then
             JAVA_HOME=/System/Library/Frameworks/JavaVM.framework/Home
           fi
           ;;
esac

#
# Get the VFSH_HOME variable
#
if [ -z "$VFSH_HOME" ] ; then
  # use the location of this script to infer $VFSH_HOME
  whoami=`basename $0`
  whereami=`echo $0 | sed -e "s#^[^/]#\`pwd\`/&#"`
  whereami=`dirname $whereami`

  # Resolve any symlinks of the now absolute path, $whereami
  realpath_listing=`ls -l $whereami/$whoami`
  case "$realpath_listing" in
    *-\>\ /*)
      realpath=`echo $realpath_listing | sed -e "s#^.*-> ##"`
      ;;
    *-\>*)
      realpath=`echo $realpath_listing | sed -e "s#^.*-> #$whereami/#"`
      ;;
    *)
      realpath=$whereami/$whoami
      ;;
  esac
  VFSH_HOME=`dirname "$realpath"`/..
fi

#
# For Cygwin, ensure paths are in UNIX format before anything is touched
#
if $cygwin ; then
  [ -n "$VFSH_HOME" ] &&
    VFSH_HOME=`cygpath --unix "$VFSH_HOME"`
  [ -n "$JAVA_HOME" ] &&
    JAVA_HOME=`cygpath --unix "$JAVA_HOME"`
fi


#
# Get the Java command
#
if [ -z "$JAVACMD" ] ; then
  if [ -n "$JAVA_HOME"  ] ; then
    if [ -x "$JAVA_HOME/jre/sh/java" ] ; then
      # IBM's JDK on AIX uses strange locations for the executables
      JAVACMD="$JAVA_HOME/jre/sh/java"
    else
      JAVACMD="$JAVA_HOME/bin/java"
    fi
  else
    JAVACMD=`which java 2> /dev/null `
    if [ -z "$JAVACMD" ] ; then
        JAVACMD=java
    fi
  fi
fi

if [ ! -x "$JAVACMD" ] ; then
  echo "Error: JAVA_HOME is not defined correctly."
  echo "  We cannot execute $JAVACMD"
  exit 1
fi


#
# Get the path separator
#
if [ "$cygwin" = "true" ] ; then
  S=';'
else
  S=':'
fi


#
# Build classpath
#
if [ "$cygwin" = "true" ] ; then
    VFSH_CLASSPATH=$CLASSPATH$S`cygpath -w $VFSH_HOME/local/classes`$S`cygpath -w $VFSH_HOME/common/classes`
else
    VFSH_CLASSPATH=$CLASSPATH$S$VFSH_HOME/local/classes$S$VFSH_HOME/common/classes
fi

for i in $VFSH_HOME/local/lib/*.jar; do
  if [ "$cygwin" = "true" ] ; then
    LIB=`cygpath -w $i`
  else
    LIB=$i
  fi

  VFSH_CLASSPATH=$VFSH_CLASSPATH$S$LIB
done

for i in $VFSH_HOME/common/lib/*.jar; do
  if [ "$cygwin" = "true" ] ; then
    LIB=`cygpath -w $i`
  else
    LIB=$i
  fi

  VFSH_CLASSPATH=$VFSH_CLASSPATH$S$LIB
done


#
# Execute command
#
CURR_DIR=`pwd`
cd $VFSH_HOME
MAIN_CLASS=org.vfsutils.shell.sshd.Server
"$JAVACMD" -classpath "$VFSH_CLASSPATH" $MAIN_CLASS $@
RESULT=$?
cd $CURR_DIR
exit $RESULT
