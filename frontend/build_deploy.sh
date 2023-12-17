chmod 777 -R .

docker build -t accountant-frontend:v2-dev .  || { echo 'ERROR: docker build failed' ; exit 1; }

docker run -d -p 3301:80 accountant-frontend:v2-dev
