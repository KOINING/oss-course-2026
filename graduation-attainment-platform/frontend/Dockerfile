# 使用 Nginx 提供前端静态页面。
# dist/ 由 GitHub Actions 预先构建并同步到服务器。
FROM nginx:stable-alpine

COPY dist /usr/share/nginx/html
COPY nginx.conf /etc/nginx/conf.d/default.conf

EXPOSE 80
