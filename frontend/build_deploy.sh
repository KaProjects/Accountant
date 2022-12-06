docker build -t accountant-frontend:v1.0 .

docker run -d -p 3301:3000 \
    -v ${pwd}:/app \
    -v /app/node_modules \
    -e CHOKIDAR_USEPOLLING=true \
    accountant-frontend:v1.0