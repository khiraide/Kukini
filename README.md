Kukini - A digital records transfer tool for the Hawaii State Archives
======

At the Hawaii State Archives, there is a need to update their digital records preservation capabilities.  Thus, they are currently in the process of implementing a records system which has been designed to store, protect, and preserve digital records. The types of digital records include medical records, annual reports, birth records, etc. This records system requires a Digital Records Transfer tool which must provide government agencies of Hawaii with the ability to transfer digital records to the Hawaii State Archives. Its transfer process must use secure and authenticated methods that document and ensure that the entirety of the files have been transferred uncorrupted. Kukini is a digital records transfer tool that has been designed, implemented, tested, and evaluated for use within an archival framework. To install this system:


1. Install Java 7 and Maven
----------------

Please visit https://www.java.com/en/download/ to install Java 7. 

Start by following the [directions on installing Maven](http://maven.apache.org/download.cgi).

Be sure to run mvn --version to verify that it is correctly installed.  This package has been tested using Maven 3.0.4.


2. Install Hawaii State Archives jar files into your local Maven repository
--------------------------------------------------------------

The Hawaii State Archives binaries are not provided as part of public Maven repositories, so the next step is to install the three jar files needed for compilation and testing into your local repository.   You accomplish this by changing directory to libs and executing the following three commands:

```
mvn install:install-file -Dfile=bag-module-1.0-SNAPSHOT.jar -DartifactId=bag-module  -DgroupId=gov.hawaii.digitalarchives -Dversion=1.0-SNAPSHOT -Dpackaging=jar -DgeneratePom=true

mvn install:install-file -Dfile=persistent-id-module-1.0-SNAPSHOT.jar -DartifactId=persistent-id-module -DgroupId=gov.hawaii.digitalarchives  -Dversion=1.0-SNAPSHOT -Dpackaging=jar -DgeneratePom=true

mvn install:install-file -Dfile=space-module-1.0-SNAPSHOT.jar -DartifactId=space-module -DgroupId=gov.hawaii.digitalarchives  -Dversion=1.0-SNAPSHOT -Dpackaging=jar -DgeneratePom=true

mvn install:install-file -Dfile=hida-core-1.0-SNAPSHOT.jar -DartifactId=hida-core -DgroupId=gov.hawaii.digitalarchives  -Dversion=1.0-SNAPSHOT -Dpackaging=jar -DgeneratePom=true

```

3  Install Tomcat 7
-----------------------------
Tomcat 7 is used to deploy the SIP Transfer Servlet. Using Ubuntu, Tomcat 7 can be installed with the commmand:

```
sudo apt-get install tomcat7

```


4 Deploy the SIP Transfer Servlet
-----------------------------
Deploy the SIP Transfer Servlet war file in the libs directory. The war file is called, sipuploader.war



5  Build and test the system
-----------------------------

Now that everything is installed, build and test the system. You use the standard Maven 'install' target.

```
mvn install

```


Now to run the application, change to the application-module directory. Run the maven command:

```
mvn nbm:run-platform

```

