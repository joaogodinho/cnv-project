# cnv-project
CloudPrime project - "a web server elastic cluster that performs CPUintensive calculations on-demand"

## Installing JDK
```
wget --header "Cookie: oraclelicense=accept-securebackup-cookie" http://download.oracle.com/otn-pub/java/jdk/7u79-b15/jdk-7u79-linux-x64.tar.gz
su - 
mkdir /opt/jdk
tar -zxf jdk-7u79-linux-x64.tar.gz -C /opt/jdk
update-alternatives --install /usr/bin/java java /opt/jdk/jdk1.7.0_79/bin/java 100
update-alternatives --install /usr/bin/javac javac /opt/jdk/jdk1.7.0_79/bin/javac 100
update-alternatives --install /usr/bin/javap javap /opt/jdk/jdk1.7.0_79/bin/javap 100
# Verify with java/javac/javap -version
```

## Installing `sbt`
```
wget https://repo.typesafe.com/typesafe/ivy-releases/org.scala-sbt/sbt-launch/0.13.11/sbt-launch.jar
# Move sbt-launch.jar to ~/bin (make sure ~/bin is in $PATH, Ubuntu does this auto, just restart)
mv sbt-launch.jar ~/bin
# Create file ~/bin/sbt with the following:
#!/bin/bash
SBT_OPTS="-Xms512M -Xmx1536M -Xss1M -XX:+CMSClassUnloadingEnabled -XX:MaxPermSize=256M"
java $SBT_OPTS -jar `dirname $0`/sbt-launch.jar "$@"
```
