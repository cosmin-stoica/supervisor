package ControllerClasses;

/*
 heylisten
 HEY!!! A NEW, BETTER AND EASIER WAY WAS FOUND!!!
 LOOK AT "Job Updater" PROJECT AND THESE STACKOVERFLOW THREADS:
 https://stackoverflow.com/questions/57019143/build-executable-jar-with-javafx11-from-maven
 https://stackoverflow.com/questions/60028267/how-to-generate-executable-jar-in-javafx-with-maven-project

 TL:DR
 If you want to use the latest JDK, then read this mini-guide. If you don't want to go trough all this hassle
 and are fine with using an older JDK, then just use JDK 1.8 (Java8) (the most popular in my opinion) or the last JDK to have JavaFx integrated: JDK 10 (Java10).

 You WILL have problems with opening the JavaFX application if using a JDK version 11+. JDK 10 was the last one to have native JavaFX support.
 Now to build and run a JavaFX jar, follow these steps:

 ( This is just me ranting, sorry!
 This mini-guide exists solely because Oracle hates JavaFX, and if a class that extends
 application is launched (such as the App class, which is the JFX class),
 it will not run properly and give the error [JavaFX runtime components are missing] when running
 from CLI with command [java -jar YOUR_JAR_NAME.jar].
 )

BUILD:
 So, to deploy the program and build a .jar file, these passages need to be done:
 1) Create a Main class, that will just run the JavaFX main class, the one that extends Application;
 2) Create a jar artifact, by going File>Project Structure>Artifacts, then Add(+)>JAR>From modules with dependencies;
 3) As the main class choose the class created at step 1, the Main class, select option extract to the target jar;
 4) Exit by clicking OK;
 5) Generate the artifact by going to Build>Build artifacts>Build, the artifact will be found in \out\artifacts\Supervisor_jar.

RUN:
 To run your JavaFX app, you can't just double click it. It has to be done from CLI by using this command:
 java -jar YOUR_JAR_NAME.jar
 It is recommended to make a script file (.bat) that will run this command instead of opening CLI and typing it everytime.

 P.S. Remember to check the JAVA_HOME and the java jdk path in the environmental variable in the windows settings, it should be the same as the JDK used.
 If it's not, then follow this guide, only step 2 is mandatory if you already have the JDK installed:
 https://www.happycoders.eu/java/how-to-switch-multiple-java-versions-windows/
 */
public class SuperVisorMain
{
    public static void main(String[] args)
    {
        SuperVisorApp.main(args);
}
}
