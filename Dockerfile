FROM tomcat:8-alpine

MAINTAINER "denklewer@gmail.com"

RUN ["rm", "-fr", "/usr/local/tomcat/webapps/ROOT"]

COPY target/ROOT.war /usr/local/tomcat/webapps/guacamole.war

EXPOSE 8080

CMD ["catalina.sh", "run"]
