FROM node:24-alpine

WORKDIR /usr/src/app

RUN npm install -g @angular/cli

COPY package*.json ./

RUN npm ci

COPY . .

ENV PORT=4200

EXPOSE 4200

CMD ["sh", "-c", "ng serve --port ${PORT:-4200} --host 0.0.0.0"]