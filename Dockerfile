From openjdk:8
copy ./target/bookstore-0.0.1-SNAPSHOT.jar bookstore-0.0.1-SNAPSHOT.jar
CMD ["java","-jar","bookstore-0.0.1-SNAPSHOT.jar"]