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
// Verify with java/javac -version
```
