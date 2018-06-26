# HtmlAnalysisApplication

This application will take an url starting with http:// or https:// and will analyze the DOM .


### Environment Set up
* Java 8
* Library
  * Jsoup (HTML Parser)
  * Guava (For finding sublists from a main list)
* Spring-boot
* Thymeleaf
* Bootstrap (CSS)

### How to build and run the Application ? 

* Clone the git repository
* Get into the HtmlAnalysisApplication directory using ```cd``` command
* Build the application . Execute below command
  ```$./mvnw clean install ```
* Execute the below command and visit ```http://localhost:8080``` from web browser
       ```$java -jar Scout24App.jar``````$java -jar Scout24App.jar```


NOTE: 
 1. Application will ask for a credential type username and password as user and user respectively.
 2. All the application constraints are written on Constraints.txt under src/resources
 3. If you are running the application behind any proxy , then add the proxy as a JVM parameter. Refer this [link](https://docs.oracle.com/javase/8/docs/technotes/guides/net/proxies.html)  
 4. If you want to execute this application, Dockerfile is attached. You can build docker container and run it

