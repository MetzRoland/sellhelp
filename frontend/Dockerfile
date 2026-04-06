FROM node:24 AS build
WORKDIR /frontend
COPY . .
RUN npm install && npm run build

FROM nginx:alpine

COPY --from=build /frontend/dist /usr/share/nginx/html
COPY nginx.conf /etc/nginx/conf.d/default.conf
