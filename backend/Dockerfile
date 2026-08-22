FROM eclipse-temurin:17-jre
WORKDIR /app
COPY build/libs/*-all.jar app.jar
EXPOSE 8080
CMD ["java", "-jar", "app.jar"]
# Deploy: Cloud Run — 2M req/mo free tier, but billing enabled + budget alerts
# gcloud run deploy repforge-backend --source . --region us-central1 --allow-unauthenticated
