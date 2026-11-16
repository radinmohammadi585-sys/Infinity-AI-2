FROM eclipse-temurin:21-jdk
WORKDIR /app
COPY . .
RUN mkdir -p out && javac -encoding UTF-8 -d out *.java
EXPOSE 10000
CMD ["java", "-cp", "out", "com.infinity.ai2.ServerMain"]
