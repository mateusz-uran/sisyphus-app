FROM node:18 AS build-stage

WORKDIR /app

COPY package*.json ./

RUN npm install

COPY . .

## ng build --configuration production
RUN npm run build --prod

FROM nginx:alpine

COPY nginx.conf /etc/nginx/nginx.conf

COPY --from=build-stage /app/dist/sisyphus-app-frontend/browser /usr/share/nginx/html

EXPOSE 80
