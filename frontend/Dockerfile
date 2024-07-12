FROM node:18 AS build-stage

RUN npm install -g @angular/cli

WORKDIR /app

COPY package*.json ./

RUN npm install

COPY . .

# RUN npm run build --prod
RUN ng build --configuration production

FROM nginx:alpine

COPY nginx.conf /etc/nginx/nginx.conf

COPY --from=build-stage /app/dist/sisyphus-app-frontend/browser /usr/share/nginx/html

EXPOSE 80
