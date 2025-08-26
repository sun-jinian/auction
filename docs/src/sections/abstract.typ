#import "../lib.typ":*

#set align(horizon)

= Abstract

/ Overview: The source code can be found #link("https://github.com/sun-jinian/auction")[on Github] -- for a web server that handles an auction management system. A user is able to register, log in, create an item, create an auction, and make an offer. Users can create an auction using items they have created.

There are two subprojects: a (pure) *HTML version*, which is structured as a series of separate webpages; and a *RIA version*, which is structured as a single-page webapp. The functionalities are quite the same, the code changes mostly at a frontend level. 

/ Tools: This project was built using the following technologies: 
#text(fill: rgb("#5283A2"), weight: "bold", "Java") for the backend server, leveraging Jakarta's Servlet API; 
#text(fill: rgb("#ae8e26"), weight: "bold", "Apache Tomcat") to run the server; 
for the HTML version, #text(fill: rgb("#005F0F"), weight: "bold", "Thymeleaf"), a template engine; 
and for the RIA version, #text(fill: rgb("#dcca3f"), weight: "bold")[JavaScript].

We decided to use #text(fill: rgb("#192C5F"), weight: "bold")[MySQL] version 8.0

This document has been typeset with #text(fill: eastern, weight: "bold")[Typst]. To create sequence diagrams we use `chronos` package.

/ Configuration & Running: In order to run this project, the following packages and their respective versions are to be installed:
#columns[
  - Java JDK 23
  - Apache Maven
  - Apache Tomcat 10
  - MySQL
  - JDBC driver
]

The credentials are stored in plain text in the database, while the items' images are stored on the same disk where your Tomcat server is running.