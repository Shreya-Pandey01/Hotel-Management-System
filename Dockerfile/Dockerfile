FROM eclipse-temurin:17-jdk

WORKDIR /app

COPY Src /app/Src
COPY Lib /app/Lib

RUN mkdir -p /app/out && javac -cp "Lib/*" -d out Src/*.java

CMD ["sh", "-c", "java -cp \"out:Lib/*\" Server"]