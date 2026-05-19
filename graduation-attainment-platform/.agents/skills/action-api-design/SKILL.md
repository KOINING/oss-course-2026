---
name: action-api-design
description: 为当前仓库的前后端接口设计、接口命名收敛、接口联调排障提供统一规范。用于新增或修改 Spring Boot Controller、Vue 前端 src/api 请求封装、Nginx /api 反向代理规则时，默认采用动作式 URL 命名：路径显式表达“动作 + 业务对象”，而不是仅依赖 GET/POST/PUT/DELETE 表达语义。适用于登录、用户、课程、导入、导出、计算、查询等整个项目范围的接口设计与重构，并要求同时校验前端调用路径、后端映射路径、Nginx 代理路径是否一致。
---

# Action API Design

## 概要

在本项目中，前后端接口默认采用**动作式 URL 命名**。

将业务动作直接写入 URL，不要只写资源名并依赖 HTTP 方法猜测语义。默认让路径本身表达“做什么”和“作用于谁”。

优先采用以下形态：

```text
/api/{module}/{actionTarget}
```

例如：

```text
/api/auth/login
/api/auth/getUserInfo
/api/user/createUser
/api/user/updateUser
/api/user/deleteUser
/api/user/assignRole
/api/course/importCourse
/api/course/updateCourse
/api/report/exportReport
/api/achievement/calculateAchievement
```

## 核心规则

### 1. 统一采用动作式命名

- 为整个项目默认使用动作式 URL，不限于后台管理接口。
- 让 URL 显式表达业务动作和业务对象，不要只写 `/api/user`、`/api/course` 这类宽泛资源路径。
- 后续同类接口继续沿用同一风格，不混用资源式与动作式两套命名。

### 2. 优先使用“动词 + 对象”的 camelCase 末段

- 将路径的最后一段写成完整动作短语。
- 优先使用 `createUser`、`updateUser`、`deleteUser`、`assignRole`、`importCourse`、`exportReport`、`calculateAchievement` 这一类形式。
- 前半段用于模块归类，最后一段用于表达具体动作。

### 3. 不要只依赖 HTTP 方法表达语义

- 继续保留 GET / POST / PUT / DELETE 的技术语义。
- 但默认不要把“语义全靠 HTTP 方法”作为本项目接口设计方式。
- 让 URL 本身足够直观，便于前后端协作、文档沟通、代理排查和联调测试。

## 前后端对齐规则

### 1. 对齐前端请求与后端映射

- 先同时查看前端 `src/api/*` 请求封装和后端 Controller 映射。
- 如果前端 baseURL 为 `/api`，则前端相对路径必须与后端动作式路径严格对齐。

例如：

```js
request.post('/user/createUser')
```

对应：

```java
@RequestMapping("/api/user")
@PostMapping("/createUser")
```

最终外部请求应为：

```text
/api/user/createUser
```

### 2. 使用当前仓库的现有模式作为基线

- 前端 `request.js` 负责共享 baseURL。
- 前端 `src/api/*` 只写相对业务路径。
- 后端 Controller 采用类级路径 + 方法级路径组合。
- 部署层通过 Nginx 转发 `/api/`。

设计新接口时，在这套结构上延续动作式命名，不另起一套风格。

## Nginx 兼容规则

### 1. 保留 `/api` 前缀

- 在 `location /api/` 下代理后端时，保留 `/api` 前缀。
- 主动避免：

```nginx
location /api/ {
  proxy_pass http://backend_server/;
}
```

因为这种写法会剥掉 `/api` 前缀，导致：

```text
/api/auth/login
```

被错误转发成：

```text
/auth/login
```

优先使用可保留前缀的写法：

```nginx
location /api/ {
  proxy_pass http://backend_server;
}
```

### 2. 单独处理运行级特殊路径

以下路径不作为业务接口动作式命名样板，可作为例外单独配置：

- `/health`
- `/swagger-ui`
- `/api-docs`

## 使用流程

在处理本项目接口设计或排障时，按以下顺序执行：

1. 定位当前前端 API 调用路径。
2. 定位当前后端 Controller 映射路径。
3. 判断所属模块与具体业务动作。
4. 按 `模块 + 动作对象` 形式生成或收敛 URL。
5. 校验前端路径、后端路径、Nginx `/api` 转发是否一致。
6. 如果发现现有命名与项目规范不一致，默认收敛到动作式 URL 风格。
7. 只有用户显式要求其他风格时，才偏离该默认规范。

## 排障提示

当接口出现 404 / 405 / 500 时，优先检查：

1. 前端请求路径是否遗漏 `/api`。
2. 后端 `@RequestMapping` 与 `@GetMapping` / `@PostMapping` 是否拼接成预期动作式路径。
3. Nginx `/api/` 是否错误剥离了 `/api` 前缀。
4. 当前运行分支与服务器部署分支是否一致。
5. 修改 `deploy/nginx.conf` 后是否重新加载或重启了 Nginx 容器。

